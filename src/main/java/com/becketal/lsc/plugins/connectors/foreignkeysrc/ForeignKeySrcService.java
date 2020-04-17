package com.becketal.lsc.plugins.connectors.foreignkeysrc;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;

import org.lsc.LscDatasets;
import org.lsc.beans.IBean;
import org.lsc.configuration.LscConfiguration;
import org.lsc.configuration.PluginSourceServiceType;
import org.lsc.configuration.ServiceType;
import org.lsc.configuration.TaskType;
import org.lsc.configuration.TaskType.AuditLog;
import org.lsc.exception.LscServiceConfigurationException;
import org.lsc.exception.LscServiceException;
import org.lsc.service.IService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.becketal.lsc.plugins.connectors.foreignkeysrc.generated.ForeignKeySrcServiceConfig;
import com.becketal.lsc.plugins.connectors.foreignkeysrc.generated.SourceType;

public class ForeignKeySrcService implements IService {

	protected static final Logger LOGGER = LoggerFactory.getLogger(ForeignKeySrcService.class);

	protected IService keySourceService;
	protected IService dataSourceService;
	protected ServiceType dataService;
	protected ServiceType keyService;
	protected TaskType dataTaskType;
	protected TaskType keyTaskType;

	protected boolean useKeyData;

	private final Class<IBean> beanClass;
	
	@SuppressWarnings("unchecked")
	public ForeignKeySrcService(final TaskType task) throws LscServiceConfigurationException {
		// get the plug-in configuration
		// try to get my configuration. Should be of type databaseConnection
		if (task != null) {
			try {
				beanClass = (Class<IBean>) Class.forName(task.getBean());

				PluginSourceServiceType pluginSourceServiceType = task.getPluginSourceService();
				if (pluginSourceServiceType != null) {
					// find our configuration
					ForeignKeySrcServiceConfig config = null;
					for (Object o : task.getPluginSourceService().getAny()) {
						LOGGER.debug("getAny Object Type is : " + o.getClass().getName());
						if (o instanceof ForeignKeySrcServiceConfig) {
							config = (ForeignKeySrcServiceConfig)o;
							break;
						}
					}
					if (config != null) {

						// Continue to read the configuration from the pluginSourceServiceType
						LOGGER.debug("configType is " + pluginSourceServiceType.getClass().getName());
						LOGGER.debug("Read config from " + pluginSourceServiceType.getName());

						// useKeyData
						useKeyData = config.isUseKeyData();

						// Initialize key source
						SourceType keySource = config.getKeySource();
						keyService = getSourceService(keySource);
						if (keyService == null)
							throw new LscServiceConfigurationException("Missing source key service for plugin=" + pluginSourceServiceType.getName());
						keyTaskType = createFakeTaskType(task, keySource);

						// Initialize data source
						SourceType dataSource = config.getDataSource();
						dataService = getSourceService(dataSource);
						if (dataService == null)
							throw new LscServiceConfigurationException("Missing source data service for plugin=" + pluginSourceServiceType.getName());
						dataTaskType = createFakeTaskType(task, dataSource);

						// Instantiate key source service and pass parameters
						Constructor<?> constrKeySrcService = LscConfiguration.getServiceImplementation(keyService).getConstructor(new Class[]{TaskType.class});
						keySourceService = (IService) constrKeySrcService.newInstance(new Object[]{keyTaskType});
						LOGGER.debug("keySourceService is " + keySourceService.getClass().getName());

						// Instantiate data source service and pass parameters
						Constructor<?> constrDataSrcService = LscConfiguration.getServiceImplementation(dataService).getConstructor(new Class[]{TaskType.class});
						dataSourceService = (IService) constrDataSrcService.newInstance(new Object[]{dataTaskType});
						LOGGER.debug("dataSourceService is " + dataSourceService.getClass().getName());


					} else {
						LOGGER.debug("ForeignKeySrcServiceConfig not found");
						throw new LscServiceConfigurationException("Unable to identify the ForeignKeySrcService service configuration " + "inside the plugin source node of the task: " + task.getName());
					}
				} else {
					LOGGER.debug("pluginSourceServiceType not found");
					throw new LscServiceConfigurationException("Unable to identify the pluginSourceServiceType service configuration " + "inside the plugin source node of the task: " + task.getName());
				}
			} catch (Exception e) {
				LOGGER.error("Exception during ForeignKeySrcService initialisation");
				if (LOGGER.isDebugEnabled())
					e.printStackTrace();
				throw new LscServiceConfigurationException(e.getCause().getMessage());
			}
		} else {
			LOGGER.debug("task object is null");
			throw new LscServiceConfigurationException("task object is null");
		}
	}

	public IBean getBean(String pivotName, LscDatasets pivotAttributes, boolean fromSameService)
			throws LscServiceException {
		IBean dataIBean = null;
		if (fromSameService) {
			Map<String, Object> internalDataset = null;

			if (useKeyData) {
				internalDataset = pivotAttributes.getDatasets();

				IBean keyIBean = keySourceService.getBean(pivotName, pivotAttributes, fromSameService);
				if (keyIBean != null) {
					// add the found data to our resulting dataset
					internalDataset.putAll(keyIBean.datasets().getDatasets());
				} else {
					LOGGER.debug("keySourceService entry is null. Data is igrnored.");
				}
			}

			dataIBean = dataSourceService.getBean(pivotName, pivotAttributes, fromSameService);
			if (dataIBean == null) {
				LOGGER.info("NO data fetched for " + pivotName + ". Create empty dataset.");
				// need to create an empty iBean of type 'iBeanType';
				try {
					dataIBean = beanClass.newInstance();
					dataIBean.setMainIdentifier(pivotName);
					dataIBean.setDatasets(pivotAttributes);
				} catch (Exception e) {
					LOGGER.error("Failed to receate new bean of type " + beanClass.getClass().getName());
					if (LOGGER.isDebugEnabled())
						e.printStackTrace();
				}
			}
			if (useKeyData) {
				// add the found data to our resulting dataset. Only necessary when keyData should be used.
				internalDataset.putAll(dataIBean.datasets().getDatasets());

				// add the resulting dataset to our return IBean
				dataIBean.setDatasets(new LscDatasets(internalDataset));
			}
		} else {
			// fromSameService is false, so it is the clean task.
			// only the keySourceService is used.
			dataIBean = keySourceService.getBean(pivotName, pivotAttributes, fromSameService);
		}
		return dataIBean;
	}

	public Map<String, LscDatasets> getListPivots() throws LscServiceException {
		LOGGER.debug("getListPivots is called");
		Map<String, LscDatasets> result =  keySourceService.getListPivots();
		return result;
	}


	// copy from org.lsc.configuration.LscConfiguration
	public static ServiceType getSourceService(SourceType t) {
		if(t.getAsyncLdapSourceService() != null) {
			return t.getAsyncLdapSourceService();
		} else if (t.getLdapSourceService() != null) {
			return t.getLdapSourceService();
		} else if (t.getGoogleAppsSourceService() != null) {
			return t.getGoogleAppsSourceService();
		} else if (t.getDatabaseSourceService() != null) {
			return t.getDatabaseSourceService();
		} else if (t.getPluginSourceService() != null) {
			return t.getPluginSourceService();
		}
		return null;
	}

	/**
	 * Create a fake TaskType by copying everything from the TaskType given to the plug in but replace the 
	 * Data souceTypes with the specific sourceTypes
	 * @param sourceTask
	 * @param source
	 * @return
	 */
	public TaskType createFakeTaskType(TaskType sourceTask, SourceType source) {
		TaskType result = new TaskType();
		result.setName(sourceTask.getName());
		result.setBean(sourceTask.getBean());
		result.setCleanHook(sourceTask.getCleanHook());
		result.setSyncHook(sourceTask.getSyncHook());
		result.setDatabaseDestinationService(sourceTask.getDatabaseDestinationService());
		result.setGoogleAppsDestinationService(sourceTask.getGoogleAppsDestinationService());
		result.setLdapDestinationService(sourceTask.getLdapDestinationService());
		result.setMultiDestinationService(sourceTask.getMultiDestinationService());
		result.setXaFileDestinationService(sourceTask.getXaFileDestinationService());
		result.setPluginDestinationService(sourceTask.getPluginDestinationService());
		result.setPropertiesBasedSyncOptions(sourceTask.getPropertiesBasedSyncOptions());
		result.setForceSyncOptions(sourceTask.getForceSyncOptions());
		result.setPluginSyncOptions(sourceTask.getPluginSyncOptions());
		result.setCustomLibrary(sourceTask.getCustomLibrary());
		result.setScriptInclude(sourceTask.getScriptInclude());
		result.setId(sourceTask.getId());
		// copy auditlog List
		List<AuditLog> srcList = sourceTask.getAuditLog();
		List<AuditLog> destList = result.getAuditLog();
		// destList is always empty as it is a new instance.
		if (destList != null && srcList != null)
			destList.addAll(srcList);

		// Source Type specific settings
		result.setDatabaseSourceService(source.getDatabaseSourceService());
		result.setGoogleAppsSourceService(source.getGoogleAppsSourceService());
		result.setLdapSourceService(source.getLdapSourceService());
		result.setAsyncLdapSourceService(source.getAsyncLdapSourceService());
		result.setPluginSourceService(source.getPluginSourceService());

		return result;
	}

}
