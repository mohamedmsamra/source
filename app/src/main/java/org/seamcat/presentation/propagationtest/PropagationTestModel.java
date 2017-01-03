package org.seamcat.presentation.propagationtest;

import org.seamcat.model.factory.SeamcatFactory;
import org.seamcat.model.generic.ProxyHelper;
import org.seamcat.model.systems.generic.LocalEnvironments;
import org.seamcat.plugin.PropagationModelConfiguration;

public class PropagationTestModel {

    private PropagationModelConfiguration pm;
    private LinkResultConfiguration linkResultConfiguration;
    private CommonConfiguration commonConfiguration;
    private LocalEnvironments localEnvironments;

    public PropagationTestModel() {
        pm = SeamcatFactory.propagation().getHataSE21();
        linkResultConfiguration = ProxyHelper.newInstance( LinkResultConfiguration.class );
        commonConfiguration = ProxyHelper.newInstance( CommonConfiguration.class );
    }

    @Override
    public String toString() {
        return pm.toString() + " (variations = "+pm.isVariationSelected()+")";
    }

    public PropagationModelConfiguration getPropagationModel() {
        return pm;
    }

    public void setPropagationModelConfiguration( PropagationModelConfiguration pm ) {
        this.pm = pm;
    }

    public LinkResultConfiguration getLinkResultConfiguration() {
        return linkResultConfiguration;
    }

    public void setLinkResultConfiguration( LinkResultConfiguration linkResultConfiguration ) {
        this.linkResultConfiguration = linkResultConfiguration;
    }

    public CommonConfiguration getCommonConfiguration() {
        return commonConfiguration;
    }

    public void setCommonConfiguration( CommonConfiguration commonConfiguration ) {
        this.commonConfiguration = commonConfiguration;
    }

    public LocalEnvironments getLocalEnvironments() {
        return localEnvironments;
    }

    public void setLocalEnvironments(LocalEnvironments localEnvironments) {
        this.localEnvironments = localEnvironments;
    }
}
