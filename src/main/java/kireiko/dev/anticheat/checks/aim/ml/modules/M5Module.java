package kireiko.dev.anticheat.checks.aim.ml.modules;

import kireiko.dev.millennium.math.Simplification;
import kireiko.dev.millennium.ml.data.ResultML;
import kireiko.dev.millennium.ml.data.module.FlagType;
import kireiko.dev.millennium.ml.data.module.ModuleML;
import kireiko.dev.millennium.ml.data.module.ModuleResultML;

public class M5Module implements ModuleML {
    @Override
    public String getName() {
        return "m5";
    }

    @Override
    public ModuleResultML getResult(ResultML resultML) {
        ResultML.CheckResultML checkResult = resultML.statisticsResult;
        double ab1 = checkResult.UNUSUAL;
        double ab2 = checkResult.STRANGE;
        double ab3 = checkResult.SUSPECTED;
        double ab4 = checkResult.SUSPICIOUSLY;
        FlagType type = FlagType.NORMAL;
        double percent = Math.min(Simplification.scaleVal((ab1 / 1.25) * 100, 2), 100.0);
        if (ab1 > 0.95 || (ab1 > 0.7 && ab2 > 0.345 && ab3 > 0.125 && ab4 > 0.035) && percent > 77) {
            type = FlagType.SUSPECTED;
        } else if (ab1 > 0.825 && percent > 77) {
            type = FlagType.SUSPECTED;
        } else if (ab1 > 0.475 && ab2 < 0.145 && ab3 > 0.1) {
            type = FlagType.STRANGE;
        } else if (ab1 < 0.25 && ab2 < 0.033 && ab2 != 0 && ab3 == 0 && ab4 == 0) {
            type = FlagType.STRANGE;
        }
        return new ModuleResultML(15, type, checkResult.toString());
    }
}
