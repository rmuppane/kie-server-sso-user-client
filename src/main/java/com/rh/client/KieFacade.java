package com.rh.client;

import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.client.CaseServicesClient;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.client.UIServicesClient;
import org.kie.server.client.UserTaskServicesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rh.util.RedHatSSOUtils;

public class KieFacade {

	final static Logger LOGGER = LoggerFactory.getLogger(KieFacade.class);

	private String kieServerUrl;
	private String userName;
	private String password;
	private boolean bypassAuth;

	private KieServicesClient client;
	
	
	private KieFacade(String url, String userName, String password, boolean bypassAuth) {
		this.kieServerUrl = url;
		
		this.userName = userName;
		this.password = password;
		
		this.bypassAuth = bypassAuth;
		
		this.client = getKieServicesClient();
		
	}
	
	public static KieFacade getInstance(String url, String userName, String password, boolean bypassAuth) {
		return new KieFacade(url, userName, password, bypassAuth);
	}

	public KieServicesClient getKieServicesClient() {
		LOGGER.info("Start KieServicesClient");
		System.setProperty("org.kie.server.bypass.auth.user", String.valueOf(bypassAuth));
		// LOGGER.info("com.redhat.internal.services.remoteClasses {} ", remoteClasses);
		final KieServicesConfiguration config = KieServicesFactory.newRestConfiguration(kieServerUrl, new RedHatSSOCredentialProvider(() -> RedHatSSOUtils.getInstance( userName,  password).getAccessToken()), 100000l);
		
		/*final Set<Class<?>> classes = Arrays.asList(remoteClasses.split(",")).stream().map(className -> {
			try {
				return Class.forName(className);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			return null;
		}).filter(className -> className != null).collect(Collectors.toSet());*/
		// LOGGER.info("classes Set is {} ", classes);
		// config.addExtraClasses(classes);
		config.setMarshallingFormat(MarshallingFormat.XSTREAM);
		config.setTimeout(100000l);
		LOGGER.info("End KieServicesClient");
		return KieServicesFactory.newKieServicesClient(config);
	}
	
	
	public CaseServicesClient getCaseServicesClient() {
		return client.getServicesClient(CaseServicesClient.class);
	}

	public ProcessServicesClient getProcessServicesClient() {
		return client.getServicesClient(ProcessServicesClient.class);
	}
	
	public QueryServicesClient getQueryServicesClient() {
		return client.getServicesClient(QueryServicesClient.class);
	}

	public UserTaskServicesClient getUserTaskServicesClient() {
		return client.getServicesClient(UserTaskServicesClient.class);
	}
	
	public UIServicesClient getUIServicesClient() {
	    return client.getServicesClient(UIServicesClient.class);
	}

}
