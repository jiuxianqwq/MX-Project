package kireiko.dev.anticheat.checks.aim.heuristic;

import kireiko.dev.anticheat.api.events.RotationEvent;
import kireiko.dev.anticheat.checks.aim.AimHeuristicCheck;
import kireiko.dev.millennium.math.Statistics;

public final class AimConstantCheck implements HeuristicComponent {
    private static final double MODULO_THRESHOLD = 90F;
    private static final double LINEAR_THRESHOLD = 0.1F;
    private final AimHeuristicCheck check;
    private float lastDeltaYaw = 0.0f, lastDeltaPitch = 0.0f;
    private float buffer = 0, buffer2 = 0;

    public AimConstantCheck(final AimHeuristicCheck check) {
        this.check = check;
    }

    @Override
    public void process(final RotationEvent rotationUpdate) {
        final float deltaYaw = rotationUpdate.getAbsDelta().getX();
        final float deltaPitch = rotationUpdate.getAbsDelta().getY();
        if (check.getProfile().ignoreCinematic()) return;
        { // type 1
            final long expandedPitch = (long) (Statistics.EXPANDER * deltaPitch);
            final long expandedLastPitch = (long) (Statistics.EXPANDER * lastDeltaPitch);
            final long gcd = Statistics.getGcd(expandedPitch, expandedLastPitch);
            final boolean sensitivityIsValid = check.getProfile().calculateSensitivity() > 5;
            final boolean validAngles = deltaYaw > 0.25f && deltaPitch > 0.25f && deltaPitch < 20.0f && deltaYaw < 20.0f;
            final boolean invalid = gcd < 131072L;
            if (invalid && validAngles && !sensitivityIsValid) {
                buffer = Math.min(buffer + 1, 200);
                check.getProfile().debug("&7Aim Constant (1): " + buffer);
                if (buffer > 6) {
                    check.getProfile().punish("Aim", "Heuristic", "Constant rotations (1)", 0.0f);
                    check.getProfile().setAttackBlockToTime(System.currentTimeMillis() + 4000);
                    buffer = 4;
                }
            } else if (buffer > 0) {
                buffer -= 2f;
            }
        }
        { // type 2
            final double divisorYaw = Statistics.getGcd((long) (deltaYaw * Statistics.EXPANDER), (long) (lastDeltaYaw * Statistics.EXPANDER));
            final double divisorPitch = Statistics.getGcd((long) (deltaPitch * Statistics.EXPANDER), (long) (lastDeltaPitch * Statistics.EXPANDER));
            final double constantYaw = divisorYaw / Statistics.EXPANDER;
            final double constantPitch = divisorPitch / Statistics.EXPANDER;
            final double currentX = deltaYaw / constantYaw;
            final double currentY = deltaPitch / constantPitch;
            final double previousX = lastDeltaYaw / constantYaw;
            final double previousY = lastDeltaPitch / constantPitch;
            if (deltaYaw > 0.0 && deltaPitch > 0.0 && deltaYaw < 20.0f && deltaPitch < 20.0f) {
                final double moduloX = currentX % previousX;
                final double moduloY = currentY % previousY;
                final double floorModuloX = Math.abs(Math.floor(moduloX) - moduloX);
                final double floorModuloY = Math.abs(Math.floor(moduloY) - moduloY);
                final boolean invalidX = moduloX > MODULO_THRESHOLD && floorModuloX > LINEAR_THRESHOLD;
                final boolean invalidY = moduloY > MODULO_THRESHOLD && floorModuloY > LINEAR_THRESHOLD;

                if (invalidX && invalidY) {
                    buffer2 = Math.min(buffer2 + 1, 200);
                    check.getProfile().debug("&7Aim Constant (2): " + buffer2);
                    if (buffer2 > 6) {
                        check.getProfile().punish("Aim", "Heuristic", "Constant rotations (2)", 0.0f);
                        check.getProfile().setAttackBlockToTime(System.currentTimeMillis() + 4000);
                        buffer2 = 4;
                    }
                } else if (buffer2 > 0) {
                    buffer2 -= 2f;
                }
            }
        }

        this.lastDeltaYaw = deltaYaw;
        this.lastDeltaPitch = deltaPitch;
    }
}