package kireiko.dev.anticheat.checks.aim.ml.modules;

import kireiko.dev.millennium.ml.data.ResultML;
import kireiko.dev.millennium.ml.data.module.FlagType;
import kireiko.dev.millennium.ml.data.module.ModuleML;
import kireiko.dev.millennium.ml.data.module.ModuleResultML;

public class MHuge2Module implements ModuleML {
    @Override
    public String getName() {
        return "m_huge2";
    }

    @Override
    public ModuleResultML getResult(ResultML resultML) {
        ResultML.CheckResultML checkResult = resultML.statisticsResult;
        double ab1 = checkResult.UNUSUAL;
        double ab2 = checkResult.STRANGE;
        double ab3 = checkResult.SUSPECTED;
        double ab4 = checkResult.SUSPICIOUSLY;
        FlagType type = FlagType.NORMAL;
        if (ab1 > 0.5) {
            type = FlagType.SUSPECTED;
        } else if (ab1 > 0.41 && ab2 < 0.042) {
            type = FlagType.STRANGE;
        } else if (ab4 > 0 && ab2 > 0.17 && ab1 > 0.25) {
            type = FlagType.UNUSUAL;
        }
        return new ModuleResultML(30, type, checkResult.toString());
    }
}
