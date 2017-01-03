package org.seamcat.model;

import com.rits.cloning.Cloner;
import org.apache.log4j.Logger;
import org.seamcat.cdma.CDMALinkLevelData;
import org.seamcat.function.BlockingMaskImpl;
import org.seamcat.function.EmissionMaskImpl;
import org.seamcat.interfaces.Dispatcher;
import org.seamcat.interfaces.LibraryVisitor;
import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.factory.Model;
import org.seamcat.model.factory.SeamcatFactory;
import org.seamcat.model.functions.BlockingMask;
import org.seamcat.model.functions.EmissionMask;
import org.seamcat.model.generic.ProxyHelper;
import org.seamcat.model.plugin.antenna.AntennaGainPlugin;
import org.seamcat.model.plugin.coverageradius.CoverageRadiusPlugin;
import org.seamcat.model.plugin.eventprocessing.EventProcessingPlugin;
import org.seamcat.model.plugin.propagation.PropagationModelPlugin;
import org.seamcat.model.systems.SystemModel;
import org.seamcat.model.systems.cdma.SystemModelCDMADownLink;
import org.seamcat.model.systems.cdma.SystemModelCDMAUpLink;
import org.seamcat.model.systems.generic.ReceiverModel;
import org.seamcat.model.systems.generic.SystemModelGeneric;
import org.seamcat.model.systems.generic.T_ReceiverModel;

import org.seamcat.model.systems.generic.TransmitterModel;
import org.seamcat.model.systems.ofdma.SystemModelOFDMADownLink;
import org.seamcat.model.systems.ofdma.SystemModelOFDMAUpLink;
import org.seamcat.model.types.*;
import org.seamcat.plugin.*;
import org.seamcat.simulation.generic.CognitiveRadio;

import java.util.*;

public class Library implements LibraryVisitor<List<? extends LibraryItem>> {

    private static final Logger LOG = Logger.getLogger(Library.class);

    private final List<ReceiverModel> receivers;
    private final List<T_ReceiverModel> t_receivers;
    private final List<TransmitterModel> transmitters;
    private final List<CDMALinkLevelData> cdmalinklevel;
    private final List<EmissionMaskImpl> spectrumEmissionMasks;
    private final List<BlockingMaskImpl> receiverBlockingMasks;
    private final List<SystemModel> systems;
    private final Set<String> installedJars;
    private final List<PluginConfiguration> configurations= new ArrayList<PluginConfiguration>();

    public void ensureConfiguration(JarConfigurationModel jarConfigurationModel) {
        // ensure at least one instance of each plugin class
        List<PluginConfiguration> forJar = getConfigurationsForJar(jarConfigurationModel.getHash());
        for (PluginClass pc : jarConfigurationModel.getPluginClasses()) {
            if ( !contains(forJar, pc)) {
                configurations.add( pc.configuration() );
            }
        }
    }

    public List<PluginConfiguration> getConfigurationsForJar( String hash ) {
        List<PluginConfiguration> list = new ArrayList<PluginConfiguration>();
        JarConfigurationModel jarConfiguration = PluginJarFiles.getJarConfiguration(hash);
        if ( jarConfiguration != null ) {
            for (PluginClass aClass : jarConfiguration.getPluginClasses()) {
                for (PluginConfiguration configuration : configurations) {
                    if ( configuration.getLocation().equals( aClass.getPluginLocation() ) ) {
                        list.add(configuration);
                    }
                }
            }
        }
        return list;
    }

    private boolean contains(List<PluginConfiguration> forJar, PluginClass pc) {
        for (PluginConfiguration configuration : forJar) {
            if (configuration.getPluginClass().getName().equals( pc.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public Library() {
        this( new HashSet<String>(), new ArrayList<ReceiverModel>(), new ArrayList<T_ReceiverModel>(),new ArrayList<TransmitterModel>(),
                new ArrayList<CDMALinkLevelData>(), new ArrayList<EmissionMaskImpl>(),
                new ArrayList<BlockingMaskImpl>(), new ArrayList<SystemModel>(), new ArrayList<PluginConfiguration>());
    }

	public Library(Set<String> installedJars, List<ReceiverModel> receivers, List<T_ReceiverModel> t_receivers,List<TransmitterModel> transmitters,
                   List<CDMALinkLevelData> cdmalinklevel, List<EmissionMaskImpl> spectrumEmissionMasks,
                   List<BlockingMaskImpl> receiverBlockingMasks, List<SystemModel> systems, List<PluginConfiguration> pluginConfigurations ) {
        this.receivers = receivers;
        this.t_receivers = t_receivers;
        this.transmitters = transmitters;
        this.cdmalinklevel = cdmalinklevel;
        this.spectrumEmissionMasks = spectrumEmissionMasks;
        this.receiverBlockingMasks = receiverBlockingMasks;
        this.systems = systems;
        this.installedJars = installedJars;
        for (PluginConfiguration configuration : pluginConfigurations) {
            addPluginConfiguration(configuration);
        }
    }

	public boolean hasLibraryFunction( EmissionMaskImpl function ) {
        return find(function, spectrumEmissionMasks) != null;
    }
	
	public boolean hasLibraryFunction( BlockingMaskImpl function ) {
        return find(function, receiverBlockingMasks) != null;
    }

	public boolean addLibraryFunction(EmissionMaskImpl function ) {
        return add(function, spectrumEmissionMasks);
	}
	
	public boolean addLibraryFunction( BlockingMaskImpl function ) {
        return add(function, receiverBlockingMasks);
	}

	public void overrideLibraryFunction(EmissionMaskImpl function) {
        remove( function, spectrumEmissionMasks );
        add(function, spectrumEmissionMasks );
	}

	public void overrideLibraryFunction(BlockingMaskImpl function) {
        remove( function, receiverBlockingMasks);
        add(function, receiverBlockingMasks );
	}

	public List<EmissionMaskImpl> getSpectrumEmissionMasks() {
		List<EmissionMaskImpl> result = new ArrayList<EmissionMaskImpl>();
		for (EmissionMaskImpl spectrumEmissionMask : spectrumEmissionMasks) {
			result.add(spectrumEmissionMask);
		}
		return result;
	}

    public List<SystemModel> getSystems() {
        return new ArrayList<>(systems);
    }

	public List<BlockingMaskImpl> getReceiverBlockingMasks() {
        return new ArrayList<>(receiverBlockingMasks);
	}

    public void writeAll( Class<? extends LibraryItem> type, List<? extends LibraryItem> all ) {
        if ( type == ReceiverModel.class ) {
            receivers.clear();
            for (LibraryItem item : all) {
                receivers.add((ReceiverModel) item);
                t_receivers.add((T_ReceiverModel) item);
            }
        } else if ( type == TransmitterModel.class ) {
            transmitters.clear();
            for ( LibraryItem item: all) {
                transmitters.add((TransmitterModel) item);
            }
        } else if ( type == JarConfigurationModel.class ) {
            Set<String> deleted = new HashSet<String>(installedJars);
            installedJars.clear();
            for (LibraryItem item : all) {
                JarConfigurationModel jar = (JarConfigurationModel) item;
                PluginJarFiles.addJarConfiguration( jar );
                installedJars.add(jar.getHash());
                ensureConfiguration( jar );
                deleted.remove( jar.getHash() );
            }

            if ( !deleted.isEmpty() ) {
                for (String jar : deleted) {
                    List<PluginConfiguration> instances = getConfigurationsForJar(jar);
                    for (PluginConfiguration instance : instances) {
                        configurations.remove( instance );
                    }
                }
            }
        } else if ( type == SystemModel.class ) {
            systems.clear();
            for (LibraryItem item : all) {
                systems.add((SystemModel) item);
            }
        } else if ( Configuration.class.isAssignableFrom(type)) {
            List<PluginConfiguration> removed = new ArrayList<>();
            for (PluginConfiguration conf : configurations) {
                if ( conf.getTypeClass().isAssignableFrom( type)) {
                    removed.add( conf );
                }
            }
            configurations.removeAll( removed );
            for (LibraryItem item : all) {
                configurations.add((PluginConfiguration) item);
            }
        } else if ( type == BlockingMask.class ) {
            receiverBlockingMasks.clear();
            for (LibraryItem item : all) {
                receiverBlockingMasks.add((BlockingMaskImpl) item);
            }
        } else if ( type == EmissionMask.class ) {
            spectrumEmissionMasks.clear();
            for (LibraryItem item : all) {
                spectrumEmissionMasks.add((EmissionMaskImpl) item);
            }
        } else if ( type == CDMALinkLevelData.class ) {
            cdmalinklevel.clear();
            for (LibraryItem item : all) {
                cdmalinklevel.add((CDMALinkLevelData) item);
            }
        }
    }

    public void setAll( List<? extends LibraryItem> all ) {
        if ( all == null || all.size() == 0 ) return;
        List<LibraryItem> current = (List<LibraryItem>) Dispatcher.dispatch(this, all.get(0));
        if ( current != null ) {
            current.clear();
            for (LibraryItem item : all) {
                current.add(item);
            }
        }
    }

    public void replaceNamedSystem( SystemModel model ) {
        for (int i = 0; i < systems.size(); i++) {
            SystemModel system = systems.get(i);
            if (system.description().name().equals(model.description().name())) {
                systems.remove( i );
                break;
            }
        }

        systems.add( model );
    }

    public void replacePluginInstance(PluginConfiguration configuration) {
        PluginConfiguration found = null;
        for (PluginConfiguration existing : configurations) {
            if ( existing.getClass() == configuration.getClass() && existing.description().name().equals(configuration.description().name())) {
                found = existing;
                break;
            }
        }
        int index = configurations.indexOf(found);
        configurations.remove( found );
        configurations.add( index, configuration );
    }

    public List<PluginConfiguration> getPluginConfigurations() {
        return configurations;
    }

    public List<PluginConfiguration> getPluginConfigurations( Class<? extends PluginConfiguration> clazz ) {
        List<PluginConfiguration> configurations = new ArrayList<PluginConfiguration>();
        for (PluginConfiguration configuration : this.configurations) {
            if ( clazz == configuration.getClass() )  {
                configurations.add(configuration);
            }
        }
        return configurations;
    }

    public boolean addSystem( SystemModel model) {
        for (SystemModel system : systems) {
            if ( system.description().name().equals(model.description().name())) return false;
        }

        systems.add( model );
        return true;
    }

    public boolean addPluginConfiguration( PluginConfiguration configuration ) {
        for (PluginConfiguration conf : configurations ) {
            if ( configuration.getLocation().equals(conf.getLocation()) && conf.description().name().equals(configuration.description().name()) ) {
                return false;
            }
        }

        addJarFile(configuration);

        configurations.add(configuration);
        return true;
    }

    private void ensurePluginConfiguration(PluginConfiguration configuration) {
        boolean exists = false;
        for (PluginConfiguration existing : this.configurations) {
            if ( existing.getLocation().equals(configuration.getLocation())) {
                // same jar, same class
                exists = true;
                break;
            }
        }
        if ( !exists ) {
            addPluginConfiguration( configuration );
        }
    }

    private void removeJarAndDependencies( String hash ) {
        for (PluginClass aClass : PluginJarFiles.getJarConfiguration(hash).getPluginClasses()) {
            List<PluginConfiguration> toBeRemoved = new ArrayList<PluginConfiguration>();
            for (PluginConfiguration configuration : configurations) {
                if ( configuration.getLocation().equals( new PluginLocation(hash, aClass.getClassName()))) {
                    toBeRemoved.add(configuration);
                } else {
                    // todo search for nested dependencies in any other configuration
                }
            }
            for (PluginConfiguration configuration : toBeRemoved) {
                configurations.remove( configuration );
            }
        }
        installedJars.remove(hash);
    }


    public List<JarConfigurationModel> getInstalledJars() {
        List<JarConfigurationModel> installed = new ArrayList<JarConfigurationModel>();
        for (String jar : installedJars) {
            installed.add( PluginJarFiles.getJarConfiguration( jar ));
        }
        return installed;
    }

    public void appendLoadedJarConfigurations( List<JarConfigurationModel> models ) {
        for (JarConfigurationModel model : models) {
            if ( model.getPluginClasses().size()>0) {
                PluginJarFiles.addJarConfiguration(model);
            }
        }
    }

	private <T extends LibraryItem> T findInObjects( String name, Iterable<T> items ) {
		for ( T o: items ) {
			if ( o.description().name().equalsIgnoreCase(name)) {
				return o;
			}
		}
		return null;
	}

	private <T extends LibraryItem> boolean addObject( T t, List<T> items ) {
		if ( findInObjects( t.description().name(), items )== null ) {
			items.add( t );
			return true;
		}
		return false;
	}

	private <T extends LibraryItem> void removeObject( T t, List<T> items ) {
		Object found = findInObjects(t.description().name(), items);
		if ( found != null ) {
			items.remove( t );
		}
	}

	private <T extends LibraryItem> T find( LibraryItem item, Iterable<T> items ) {
		for (T t: items) {
			if ( t.description().name().equalsIgnoreCase(item.description().name())) {
				return t;
			}
		}
		return null;
	}
    private <T extends LibraryItem> boolean add( T t, List<T> items ) {
		if ( find(t, items) == null ) {
			items.add(new Cloner().deepClone(t));
			return true;
		}
		return false;
	}

    private <T extends LibraryItem> void remove( LibraryItem item, List<T> items ) {
		T found = find(item, items);
		if ( found != null ) {
			items.remove( found );
		}
	}

	public List<ReceiverModel> getReceivers() {
		return Collections.unmodifiableList( receivers );
	}

	public List<TransmitterModel> getTransmitters() {
		return Collections.unmodifiableList(transmitters);
	}

    public List<CDMALinkLevelData> getCDMALinkLevelData() {
		return Collections.unmodifiableList( cdmalinklevel );
	}
    public boolean addReceiver( ReceiverModel receiver) {
        return addObject(receiver, receivers);
    }
    
    public boolean addTReceiver( T_ReceiverModel t_receiver) {
        return addObject(t_receiver, t_receivers);
    }

   

	public boolean addTransmitter( TransmitterModel transmitter ) {
        return addObject( transmitter, transmitters);
    }

    public boolean addCDMALinkLevelData( CDMALinkLevelData data ) {
		return addObject(data, cdmalinklevel);
	}
    public void removeCDMALinkLevelData( CDMALinkLevelData data ) {
		removeObject( data, cdmalinklevel );
	}

    @Override
    public List<? extends LibraryItem> visit(ReceiverModel receiver) {
        return receivers;
    }

    @Override
    public List<? extends LibraryItem> visit(SystemModel system) {
        return systems;
    }

    @Override
    public List<? extends LibraryItem> visit(TransmitterModel transmitter) {
        return transmitters;
    }

    @Override
    public List<? extends LibraryItem> visit(CDMALinkLevelData lld) {
        return cdmalinklevel;
    }

    @Override
    public List<? extends LibraryItem> visit(EmissionMaskImpl mask) {
        return spectrumEmissionMasks;
    }

    @Override
    public List<? extends LibraryItem> visit(BlockingMaskImpl mask) {
        return receiverBlockingMasks;
    }

    @Override
    public List<? extends LibraryItem> visit(PluginConfiguration plugin) {
        return configurations;
    }

    public void addJarFile(PluginConfiguration configuration) {
        if ( !configuration.isBuiltIn() ) {
            installedJars.add( configuration.getLocation().getJarId() );
        }
    }

    public <M extends LibraryItem> List<M> getGroup(Class<M> modelClass) {
        if ( modelClass == ReceiverModel.class ) {
            return (List<M>) receivers;
        }else if ( modelClass == T_ReceiverModel.class ) {
            return (List<M>) t_receivers;
        }  
        
        else if ( modelClass == TransmitterModel.class) {
            return (List<M>) transmitters;
        } else if ( modelClass == AntennaGain.class ) {
            List<PluginConfiguration> confs = getPluginConfigurations(AntennaGainConfiguration.class);
            List<AntennaGainConfiguration> results = new ArrayList<AntennaGainConfiguration>();
            for (PluginConfiguration conf : confs) {
                results.add((AntennaGainConfiguration) conf);
            }
            return (List<M>) results;
        } else if ( modelClass == CoverageRadius.class ) {
            List<PluginConfiguration> confs = getPluginConfigurations(CoverageRadiusConfiguration.class);
            List<CoverageRadiusConfiguration> results = new ArrayList<CoverageRadiusConfiguration>();
            for (PluginConfiguration conf : confs) {
                results.add((CoverageRadiusConfiguration) conf);
            }
            return (List<M>) results;
        } else if ( modelClass == EventProcessing.class ) {
            List<PluginConfiguration> confs = getPluginConfigurations(EventProcessingConfiguration.class);
            List<EventProcessingConfiguration> results = new ArrayList<EventProcessingConfiguration>();
            for (PluginConfiguration conf : confs) {
                results.add((EventProcessingConfiguration) conf);
            }
            return (List<M>) results;
        } else if ( modelClass == PropagationModel.class ) {
            List<PluginConfiguration> confs = getPluginConfigurations(PropagationModelConfiguration.class);
            List<PropagationModelConfiguration> results = new ArrayList<PropagationModelConfiguration>();
            for (PluginConfiguration conf : confs) {
                results.add((PropagationModelConfiguration) conf);
            }
            return (List<M>) results;
        } else if (modelClass == EmissionMask.class) {
            return (List<M>) spectrumEmissionMasks;
        } else if ( modelClass == BlockingMask.class) {
            return (List<M>) receiverBlockingMasks;
        } else if ( modelClass == JarConfigurationModel.class ) {
            List<JarConfigurationModel> jarModels = new ArrayList<JarConfigurationModel>();
            for (String jar : installedJars) {
                jarModels.add(PluginJarFiles.getJarConfiguration(jar));
            }
            return (List<M>) jarModels;
        } else if ( modelClass == CDMALinkLevelData.class ) {
            return (List<M>) cdmalinklevel;
        } else if ( modelClass == SystemModel.class ) {
            return (List<M>) systems;
        }

        throw new RuntimeException("unknown library item type: " + modelClass );
    }

    public static String typeName(Class<? extends LibraryItem> clazz) {
        if ( ReceiverModel.class.isAssignableFrom(clazz) ) {
            return "Receiver";
        } if ( T_ReceiverModel.class.isAssignableFrom(clazz) ) {
            return "t_Receiver";
        }  
        
        else if ( TransmitterModel.class.isAssignableFrom(clazz) ) {
            return "Transmitter";
        } else if ( EmissionMaskImpl.class.isAssignableFrom(clazz) ) {
            return "Spectrum Emission Mask";
        } else if ( BlockingMask.class.isAssignableFrom(clazz) ) {
            return "Receiver Blocking Mask";
        } else if ( PluginConfiguration.class.isAssignableFrom(clazz)) {
            return name((Class<? extends PluginConfiguration>) clazz);
        } else if ( JarConfigurationModel.class.isAssignableFrom(clazz) ) {
            return "Jar file";
        }

        return "unknown";
    }

    public String typeName(LibraryItem instance) {
        if ( instance instanceof ReceiverModel ) {
            return "Receiver";
        } else if ( instance instanceof T_ReceiverModel ) {
            return "t_Receiver";
        }
        
        else if ( instance instanceof TransmitterModel ) {
            return "Transmitter";
        } else if ( instance instanceof  EmissionMaskImpl ) {
            return "Spectrum Emission Mask";
        } else if ( instance instanceof BlockingMaskImpl ) {
            return "Receiver Blocking Mask";
        } else if ( instance instanceof PluginConfiguration) {
            return name((PluginConfiguration) instance);
        } else if ( instance instanceof JarConfigurationModel ) {
            return "Jar file";
        } else if ( instance instanceof CDMALinkLevelData ) {
            return "CDMA Link level data";
        } else if ( instance instanceof SystemModel ) {
            return "System";
        }

        return "unknown";
    }

    public static String name(PluginConfiguration plugin) {
        if ( plugin instanceof PropagationModelConfiguration) {
            return "Propagation Model Plugin";
        } else if ( plugin instanceof EventProcessingConfiguration) {
            return "Event Processing Plugin";
        } else if ( plugin instanceof CoverageRadiusConfiguration) {
            return "Coverage Radius Plugin";
        } else if ( plugin instanceof AntennaGainConfiguration) {
            return "Antenna Gain Plugin";
        }
        return "Unknown plugin type";
    }

    public static String name(Class<? extends PluginConfiguration> clazz ) {
        if ( clazz == PropagationModelConfiguration.class) {
            return "Propagation Model Plugin";
        } else if ( clazz == EventProcessingConfiguration.class) {
            return "Event Processing Plugin";
        } else if ( clazz == CoverageRadiusConfiguration.class) {
            return "Coverage Radius Plugin";
        } else if ( clazz == AntennaGainConfiguration.class) {
            return "Antenna Gain Plugin";
        }
        return "Unknown plugin type";
    }

    public <T extends LibraryItem> boolean add(Class<T> clazz, T instance) {
        return addObject(instance, getGroup( clazz ));
    }

    public <T extends LibraryItem> void removeItem(Class<T> clazz, String name) {
        List<T> group = getGroup(clazz);
        T found = findInObjects(name, group);
        removeObject( found, group );
    }

    public static <T extends SystemModel> Class<T> getSystemModelClass( T model ) {
        if ( model instanceof SystemModelGeneric ) {
            return (Class<T>) SystemModelGeneric.class;
        } 
        
        else if ( model instanceof SystemModelCDMAUpLink) {
            return (Class<T>) SystemModelCDMAUpLink.class;
        } else if ( model instanceof SystemModelCDMADownLink) {
            return (Class<T>) SystemModelCDMADownLink.class;
        } else if ( model instanceof SystemModelOFDMAUpLink) {
            return (Class<T>) SystemModelOFDMAUpLink.class;
        } else {
            return (Class<T>) SystemModelOFDMADownLink.class;
        }
    }

    public void ensureConsistentLibrary() {
        // ensure built-in configurations
        List<Class> builtIn = new ArrayList<>(BuiltInPlugins.getBuiltInPluginClasses().values());
        List<PluginConfiguration> configs = getPluginConfigurations();
        for (PluginConfiguration config : configs) {
            builtIn.remove(config.getPluginClass());
        }
        builtIn.remove(CognitiveRadio.class);
        for (Class aClass : builtIn) {
            if (EventProcessingPlugin.class.isAssignableFrom( aClass)) {
                addPluginConfiguration(EventProcessingConfiguration.event(aClass));
            } else if (PropagationModelPlugin.class.isAssignableFrom(aClass)) {
                addPluginConfiguration(SeamcatFactory.propagation().getByClass(aClass));
            } else if (CoverageRadiusPlugin.class.isAssignableFrom(aClass)) {
                addPluginConfiguration(CoverageRadiusConfiguration.coverage(aClass));
            } else if (AntennaGainPlugin.class.isAssignableFrom(aClass)){
                addPluginConfiguration(SeamcatFactory.antennaGain().getByClass(aClass));
            }
        }

        // ensure jar file configurations
        List<JarConfigurationModel> toBeRemoved = new ArrayList<JarConfigurationModel>();
        for (JarConfigurationModel jar : getInstalledJars()) {
            for (PluginClass pc : jar.getPluginClasses()) {
                try {
                    ensurePluginConfiguration(pc.configuration());
                } catch (AbstractMethodError error ) {
                    LOG.error("Error instantiating plugin. Removing JAR: " + jar.description().name());
                    toBeRemoved.add( jar );
                }
            }
        }
        for (JarConfigurationModel jar : toBeRemoved) {
            removeJarAndDependencies(jar.getHash());
        }

        // ensure all system types
        Set<Class> classes = new HashSet<>();
        for (SystemModel model : getSystems()) {
            classes.add(getSystemModelClass(model));
        }
        if ( !classes.contains(SystemModelGeneric.class)) {
            systems.add( Model.defaultGeneric() );
        }
       
        if (!classes.contains(SystemModelCDMAUpLink.class)) {
            systems.add( ProxyHelper.newComposite( SystemModelCDMAUpLink.class, "CDMA UpLink System" ) );
        }
        if ( !classes.contains(SystemModelCDMADownLink.class)) {
            systems.add( ProxyHelper.newComposite( SystemModelCDMADownLink.class, "CDMA DownLink System" ) );
        }
        if ( !classes.contains(SystemModelOFDMAUpLink.class)) {
            systems.add( ProxyHelper.newComposite( SystemModelOFDMAUpLink.class, "OFDMA UpLink System" ) );
        }
        if ( !classes.contains(SystemModelOFDMADownLink.class)) {
            systems.add( ProxyHelper.newComposite(SystemModelOFDMADownLink.class, "OFDMA DownLink System"));
        }
    }

    public static Distribution getFrequency( SystemModel system ) {
        if ( system instanceof SystemModelGeneric ) {
            return ((SystemModelGeneric) system).general().frequency();
        } 
        else if ( system instanceof SystemModelCDMAUpLink) {
            return ((SystemModelCDMAUpLink) system).general().frequency();
        } else if ( system instanceof SystemModelCDMADownLink) {
            return ((SystemModelCDMADownLink) system).general().frequency();
        } else if ( system instanceof SystemModelOFDMAUpLink) {
            return ((SystemModelOFDMAUpLink) system).general().frequency();
        } else if ( system instanceof SystemModelOFDMADownLink ) {
            return ((SystemModelOFDMADownLink) system).general().frequency();
        }

        throw new RuntimeException("Unknown system: " + system.description().name());
    }

	
	public List<T_ReceiverModel> getTReceivers() {
		return Collections.unmodifiableList( t_receivers );
	}

	@Override
	public List<? extends LibraryItem> visit(T_ReceiverModel receiver) {
		return t_receivers;
	}

}
