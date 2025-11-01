package kireiko.dev.anticheat.checks.aim.ml;

import kireiko.dev.anticheat.api.PacketCheckHandler;
import kireiko.dev.anticheat.api.data.ConfigLabel;
import kireiko.dev.anticheat.api.events.RotationEvent;
import kireiko.dev.anticheat.api.events.UseEntityEvent;
import kireiko.dev.anticheat.api.player.PlayerProfile;
import kireiko.dev.anticheat.core.AsyncScheduler;
import kireiko.dev.anticheat.managers.CheckManager;
import kireiko.dev.anticheat.utils.MessageUtils;
import kireiko.dev.millennium.math.Statistics;
import kireiko.dev.millennium.ml.ClientML;
import kireiko.dev.millennium.ml.FactoryML;
import kireiko.dev.millennium.ml.data.DataML;
import kireiko.dev.millennium.ml.data.ObjectML;
import kireiko.dev.millennium.ml.data.ResultML;
import kireiko.dev.millennium.ml.data.module.FlagType;
import kireiko.dev.millennium.ml.data.module.ModuleML;
import kireiko.dev.millennium.ml.data.module.ModuleResultML;
import kireiko.dev.millennium.ml.data.statistic.StatisticML;
import kireiko.dev.millennium.ml.logic.ModelML;
import kireiko.dev.millennium.vectors.Pair;
import kireiko.dev.millennium.vectors.Vec2f;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public final class AimMLCheck implements PacketCheckHandler {

    private static final boolean TEST_MODE = false;

    // 0 - prod | 1 - legit learning | 2 - hacking learning
    private static final int STATUS = 0;
    private static final Pair<Integer, String>
                    SAVE_DATA = new Pair<>(0, "plugins/MX/models/m_huge1.dat");

    private final PlayerProfile profile;
    private final List<Vec2f> rawRotations;
    private long lastAttack;
    private Map<String, Object> localCfg = new TreeMap<>();

    private int localVl = 0;

    public AimMLCheck(PlayerProfile profile) {
        this.profile = profile;
        this.rawRotations = new CopyOnWriteArrayList<>();
        this.lastAttack = 0L;
        if (CheckManager.classCheck(this.getClass()))
            this.localCfg = CheckManager.getConfig(this.getClass());
    }

    @Override
    public ConfigLabel config() {
        localCfg.put("enabled", true);
        localCfg.put("unusual_vl", 10);
        localCfg.put("strange_vl", 20);
        localCfg.put("suspected_vl", 40);
        return new ConfigLabel("aim_ml", localCfg);
    }
    @Override
    public void applyConfig(Map<String, Object> params) {
        localCfg = params;
    }

    @Override
    public Map<String, Object> getConfig() {
        return localCfg;
    }

    @Override
    public void event(Object o) {
        if (o instanceof RotationEvent) {
            if (profile.isCinematic()) return;
            if (!((boolean) getConfig().get("enabled"))) return;
            RotationEvent event = (RotationEvent) o;
            if (System.currentTimeMillis() > this.lastAttack + 2500) return;
            Vec2f delta = event.getDelta();
            this.rawRotations.add(delta);
            if (TEST_MODE) {
                profile.getPlayer().sendActionBar(this.rawRotations.size() + "/600");
            }
            if (this.rawRotations.size() >= 600) this.check();
        } else if (o instanceof UseEntityEvent) {
            UseEntityEvent event = (UseEntityEvent) o;
            if (event.isAttack()) {
                this.lastAttack = System.currentTimeMillis();
            }
        }
    }

    private void checkResult(List<ObjectML> objectML) {
        final List<ResultML> results = new ArrayList<>();
        final List<ModuleResultML> moduleResults = new ArrayList<>();
        final Set<String> modelsThatFlagged = new HashSet<>();
        ModuleResultML finalModuleResult = new ModuleResultML(0, FlagType.NORMAL, null);
        for (int i = 0; i < ClientML.MODEL_LIST.size(); i++) {
            final ResultML resultML = FactoryML.getModel(i).checkData(objectML);
            ModuleML moduleML = ClientML.MODEL_LIST.get(i);
            final ModuleResultML moduleResultML = moduleML.getResult(resultML);
            if (moduleResultML.getType() != FlagType.NORMAL) {
                modelsThatFlagged.add(moduleML.getName());
            }
            if (finalModuleResult.getInfo() == null) {
                finalModuleResult = moduleResultML;
            } else {
                final int finalLevel = finalModuleResult.getType().getLevel();
                final int tempLevel = moduleResultML.getType().getLevel();
                final boolean isLevelSmaller = finalLevel < tempLevel;
                final boolean isPrioritySmaller = finalLevel == tempLevel && finalModuleResult.getPriority() < moduleResultML.getPriority();
                if (isLevelSmaller || isPrioritySmaller) {
                    finalModuleResult = moduleResultML;
                }
            }
            if (TEST_MODE) {
                profile.getPlayer().sendMessage(i + ": " + resultML.statisticsResult.toString());
            }
            results.add(resultML);
        }
        if (TEST_MODE) {
            profile.getPlayer().sendMessage("Result: " + finalModuleResult.getType().toString());
        }
        profile.debug("&8ML Result: " + finalModuleResult.getType() + " " + finalModuleResult.getInfo());
        if (finalModuleResult.getType() != FlagType.NORMAL) {
            final FlagType type = finalModuleResult.getType();
            float vl;
            String color;
            switch (type) {
                case UNUSUAL: {
                    color = "&e";
                    vl = ((Number) localCfg.get("unusual_vl")).floatValue() / 10f;
                    break;
                }
                case STRANGE: {
                    color = "&6";
                    vl = ((Number) localCfg.get("strange_vl")).floatValue() / 10f;
                    break;
                }
                case SUSPECTED: {
                    color = "&c";
                    vl = ((Number) localCfg.get("suspected_vl")).floatValue() / 10f;
                    break;
                }
                default: {
                    color = "&a";
                    vl = 0f;
                }
            }
            profile.punish("Aim", "ML", "&fResult: " + color + type + " &8" + Arrays.toString(modelsThatFlagged.toArray()), vl);
        }
    }

    private void check() {
        final ModelML modelML = (ModelML) FactoryML.getModel(SAVE_DATA.getX());
        final List<ObjectML> objectMLStack = new ArrayList<>();
        { // compute stack
            ObjectML yaw = new ObjectML(new ArrayList<>());
            ObjectML pitch = new ObjectML(new ArrayList<>());
            for(Vec2f rot : this.rawRotations) {
                yaw.getValues().add((double) rot.getX());
                pitch.getValues().add((double) rot.getY());
            }
            objectMLStack.add(yaw);
            objectMLStack.add(pitch);
        }
        if (STATUS == 0) {
            AsyncScheduler.run(() -> {
                checkResult(objectMLStack);
            });
        } else {
            ModelML modelMLLearn = (ModelML) FactoryML.getModel(SAVE_DATA.getX());
            for (int r = 0; r < 1; r++)
                modelMLLearn.learnByData(objectMLStack, (STATUS == 2));
            modelMLLearn.saveToFile(SAVE_DATA.getY());
            int i = 0;
            for (DataML dataML : modelMLLearn.getTable()) {
                for (StatisticML entropyMl : dataML.getStatisticTable()) {
                    i += entropyMl.getParameters().size();
                }
            }
            if (TEST_MODE) {
                AsyncScheduler.run(() -> {
                    ResultML resultML = modelML.checkData(objectMLStack);
                    if (TEST_MODE) {
                        ResultML.CheckResultML s = resultML.statisticsResult;
                        profile.getPlayer().sendMessage("r: " + s.toString());
                    }
                });
            }
            profile.getPlayer().sendMessage("parameters: " + i);
        }
        this.rawRotations.clear();
    }
}
