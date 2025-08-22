package kireiko.dev.anticheat.checks.aim.ml.modules;

import kireiko.dev.millennium.math.Simplification;
import kireiko.dev.millennium.ml.data.ResultML;
import kireiko.dev.millennium.ml.data.module.FlagType;
import kireiko.dev.millennium.ml.data.module.ModuleML;
import kireiko.dev.millennium.ml.data.module.ModuleResultML;

public class M2Module implements ModuleML {
    @Override
    public String getName() {
        return "m2";
    }

    @Override
    public ModuleResultML getResult(ResultML resultML) {
        ResultML.CheckResultML checkResult = resultML.statisticsResult;
        if (checkResult.UNUSUAL > 0.5 || (checkResult.UNUSUAL > 0.4 && checkResult.SUSPECTED > 0.15)) {
            return new ModuleResultML(10, FlagType.SUSPECTED,
                            String.valueOf(Simplification.scaleVal(checkResult.UNUSUAL, 3)));
        } if (checkResult.UNUSUAL > 0.4) {
            return new ModuleResultML(10, FlagType.STRANGE,
                            String.valueOf(Simplification.scaleVal(checkResult.UNUSUAL, 3)));
        } else if (checkResult.UNUSUAL > 0.3) {
            return new ModuleResultML(10, FlagType.UNUSUAL,
                            String.valueOf(Simplification.scaleVal(checkResult.UNUSUAL, 3)));
        }
        return new ModuleResultML(0, FlagType.NORMAL,
                        String.valueOf(Simplification.scaleVal(checkResult.UNUSUAL, 3)));
    }
}
