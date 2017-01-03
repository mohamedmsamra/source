package org.seamcat.model.plugin.propagation;

import org.seamcat.model.types.PropagationModel;

public interface PropagationModelFactory {

    PropagationModel<HataInput> getHataSE21();
    PropagationModel<HataInput> getHataSE21( HataInput input, boolean variations );

    PropagationModel<HataInput> getHataSE24();
    PropagationModel<HataInput> getHataSE24( HataInput input, boolean variations );

    PropagationModel<SphericalDiffractionInput> getSphericalDiffraction();
    PropagationModel<SphericalDiffractionInput> getSphericalDiffraction(SphericalDiffractionInput input, boolean variations );

    PropagationModel<P452ver14Input> getITU_R_P_452_14();
    PropagationModel<P452ver14Input> getITU_R_P_452_14(P452ver14Input input, boolean variations);

    PropagationModel<FreespaceInput> getFreeSpace();
    PropagationModel<FreespaceInput> getFreeSpace(FreespaceInput input, boolean variations);

    PropagationModel<P1546ver4Input> getITU_R_P_1546_4land();
    PropagationModel<P1546ver4Input> getITU_R_P_1546_4land(P1546ver4Input input, boolean variations );

    PropagationModel<P1546ver1Input> getITU_R_P_1546_1Annex_8();
    PropagationModel<P1546ver1Input> getITU_R_P_1546_1Annex_8(P1546ver1Input input, boolean variations );

    PropagationModel<P1411LowAntennaHeightInput> getITU_R_P_1411();
    PropagationModel<P1411LowAntennaHeightInput> getITU_R_P_1411(P1411LowAntennaHeightInput input, boolean variations );

    PropagationModel<P528Input> getITU_R_P_528();
    PropagationModel<P528Input> getITU_R_P_528(P528Input input, boolean variations);

    PropagationModel<LongleyRice_modInput> getLongleyRice();
    PropagationModel<LongleyRice_modInput> getLongleyRice(LongleyRice_modInput input, boolean variations);

    PropagationModel<Model_C_IEEE_802_11_rev3_Input> getModelC_IEEE_802_11_rev3();
    PropagationModel<Model_C_IEEE_802_11_rev3_Input> getModelC_IEEE_802_11_rev3(Model_C_IEEE_802_11_rev3_Input input, boolean variations);

    PropagationModel<JTG56Input> getJTG56();
    PropagationModel<JTG56Input> getJTG56(JTG56Input input, boolean variations);

    <T> PropagationModel<T> getByClass( Class<? extends PropagationModelPlugin<T>> clazz);
    <T> PropagationModel<T> getByClass( Class<? extends PropagationModelPlugin<T>> clazz, T input, boolean variations );
}
