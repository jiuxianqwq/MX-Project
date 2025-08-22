package kireiko.dev.anticheat.checks.aim.ml.modules;

import kireiko.dev.millennium.math.Simplification;
import kireiko.dev.millennium.ml.data.ResultML;
import kireiko.dev.millennium.ml.data.module.FlagType;
import kireiko.dev.millennium.ml.data.module.ModuleML;
import kireiko.dev.millennium.ml.data.module.ModuleResultML;

public class M1Module implements ModuleML {
    @Override
    public String getName() {
        return "m1";
    }

    @Override
    public ModuleResultML getResult(ResultML resultML) {
        ResultML.CheckResultML checkResult = resultML.statisticsResult;
        if (checkResult.UNUSUAL > 0.8 && checkResult.STRANGE > 0.15) {
            return new ModuleResultML(20, FlagType.SUSPECTED,
                            "Blatant prohibition (" + Simplification.scaleVal(checkResult.UNUSUAL, 3) + ")");
        } else if (checkResult.UNUSUAL > 0.5 && checkResult.STRANGE > 0.11 && checkResult.SUSPICIOUSLY > 0.1) {
            return new ModuleResultML(10, FlagType.SUSPECTED,
                            "Suspected prohibition (" + Simplification.scaleVal(checkResult.UNUSUAL, 3) + ")");
        } else if (checkResult.UNUSUAL > 0.3 && checkResult.STRANGE > 0.11 && checkResult.SUSPICIOUSLY > 0.11) {
            return new ModuleResultML(15, FlagType.SUSPECTED,
                            "Suspected prohibition (" + Simplification.scaleVal(checkResult.UNUSUAL, 3) + ")");
        } else if ((checkResult.UNUSUAL > 0.45 && checkResult.STRANGE > 0.21 && checkResult.SUSPECTED > 0)) {
            return new ModuleResultML(10, FlagType.STRANGE,
                            "Strange prohibition (" + Simplification.scaleVal(checkResult.UNUSUAL, 3) + ")");
        } else if ((checkResult.UNUSUAL > 0.3 && checkResult.STRANGE > 0.085
                        && checkResult.SUSPECTED > 0.06 && checkResult.SUSPICIOUSLY > 0)) {
            return new ModuleResultML(10, FlagType.STRANGE,
                            "Strange prohibition (" + Simplification.scaleVal(checkResult.UNUSUAL, 3) + ")");
        } else if ((checkResult.UNUSUAL > 0.27 && checkResult.STRANGE > 0.06
                        && checkResult.SUSPECTED > 0.04 && checkResult.SUSPICIOUSLY > 0)) {
            return new ModuleResultML(8, FlagType.UNUSUAL,
                            "Unusual prohibition (" + Simplification.scaleVal(checkResult.UNUSUAL, 3) + ")");
        }
        return new ModuleResultML(0, FlagType.NORMAL,
                        String.valueOf(Simplification.scaleVal(checkResult.UNUSUAL, 3)));
    }
}
