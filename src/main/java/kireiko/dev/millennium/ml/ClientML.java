package kireiko.dev.millennium.ml;

import kireiko.dev.anticheat.MX;
import kireiko.dev.anticheat.checks.aim.ml.modules.*;
import kireiko.dev.millennium.ml.data.module.ModuleML;
import kireiko.dev.millennium.ml.logic.Logger;
import kireiko.dev.millennium.ml.logic.ModelVer;
import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.List;

@UtilityClass
public class ClientML {

    public static final boolean DEV_MODE = false;

    public static final String CLIENT_NAME = "001-free-complex-18k";

    public static final List<ModuleML> MODEL_LIST = Arrays.asList(
                    new MHuge1Module(),
                    new MHuge2Module(),
                    new M1Module(),
                    new M2Module(),
                    new M3Module(),
                    new M4Module(),
                    new M5Module()
    );

    public void run() {
        if (DEV_MODE) {
            for (int i = 0; i < MODEL_LIST.size(); i++) {
                FactoryML.loadFromFile(i, MODEL_LIST.get(i).getName() + ".dat", 2, 15, ModelVer.VERSION_4_5);
            }
        } else {
            for (int i = 0; i < MODEL_LIST.size(); i++) {
                FactoryML.loadFromResources(i, MODEL_LIST.get(i).getName() + ".dat", 2, 15, ModelVer.VERSION_4_5);
            }
        }
        Logger.info(CLIENT_NAME + " loaded!");
    }
}
