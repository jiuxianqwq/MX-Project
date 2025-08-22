package kireiko.dev.anticheat.checks.aim.ml.modules;

import kireiko.dev.millennium.math.Simplification;
import kireiko.dev.millennium.ml.data.ResultML;
import kireiko.dev.millennium.ml.data.module.FlagType;
import kireiko.dev.millennium.ml.data.module.ModuleML;
import kireiko.dev.millennium.ml.data.module.ModuleResultML;

public class M4Module implements ModuleML {
    @Override
    public String getName() {
        return "m4";
    }

    @Override
    public ModuleResultML getResult(ResultML resultML) {
        ResultML.CheckResultML checkResult = resultML.statisticsResult;
        if (checkResult.UNUSUAL > 0.25 && checkResult.STRANGE > 0.07 && checkResult.SUSPECTED > 0.018 && checkResult.SUSPICIOUSLY > 0) {
            return new ModuleResultML(10, FlagType.SUSPECTED,
                            String.valueOf(Simplification.scaleVal(checkResult.UNUSUAL, 3)));
        } else if (checkResult.UNUSUAL > 0.25 && checkResult.STRANGE > 0.04 && checkResult.SUSPECTED > 0) {
            return new ModuleResultML(10, FlagType.STRANGE,
                            String.valueOf(Simplification.scaleVal(checkResult.UNUSUAL, 3)));
        }
        return new ModuleResultML(0, FlagType.NORMAL,
                        String.valueOf(Simplification.scaleVal(checkResult.UNUSUAL, 3)));
    }
}
