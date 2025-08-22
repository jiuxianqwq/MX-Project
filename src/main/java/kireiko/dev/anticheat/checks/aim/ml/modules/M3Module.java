package kireiko.dev.anticheat.checks.aim.ml.modules;

import kireiko.dev.millennium.math.Simplification;
import kireiko.dev.millennium.ml.data.ResultML;
import kireiko.dev.millennium.ml.data.module.FlagType;
import kireiko.dev.millennium.ml.data.module.ModuleML;
import kireiko.dev.millennium.ml.data.module.ModuleResultML;

public class M3Module implements ModuleML {
    @Override
    public String getName() {
        return "m3";
    }

    @Override
    public ModuleResultML getResult(ResultML resultML) {
        ResultML.CheckResultML checkResult = resultML.statisticsResult;
        if (checkResult.UNUSUAL > 0.3 && checkResult.STRANGE > 0.20 && checkResult.SUSPECTED > 0.14 && checkResult.SUSPICIOUSLY > 0.07) {
            return new ModuleResultML(20, FlagType.SUSPECTED,
                            String.valueOf(Simplification.scaleVal(checkResult.UNUSUAL, 3)));
        } else if (checkResult.UNUSUAL > 0.34 && checkResult.STRANGE > 0.12 && checkResult.SUSPECTED > 0.1 && checkResult.SUSPICIOUSLY > 0) {
            return new ModuleResultML(20, FlagType.STRANGE,
                            String.valueOf(Simplification.scaleVal(checkResult.UNUSUAL, 3)));
        } else if (checkResult.UNUSUAL > 0.25 && checkResult.STRANGE > 0.12 && checkResult.SUSPECTED > 0.12) {
            return new ModuleResultML(20, FlagType.STRANGE,
                            String.valueOf(Simplification.scaleVal(checkResult.UNUSUAL, 3)));
        } else if (checkResult.UNUSUAL > 0.10 && checkResult.STRANGE > 0.14 && checkResult.SUSPECTED > 0.08 && checkResult.SUSPICIOUSLY > 0) {
            return new ModuleResultML(20, FlagType.STRANGE,
                            String.valueOf(Simplification.scaleVal(checkResult.UNUSUAL, 3)));
        }
        return new ModuleResultML(0, FlagType.NORMAL,
                        String.valueOf(Simplification.scaleVal(checkResult.UNUSUAL, 3)));
    }
}
