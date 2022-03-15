package com.rh.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KieClient {

	final static Logger LOGGER = LoggerFactory.getLogger(KieClient.class);

	private final String KIE_SERVER_URL = "http://localhost:8090/rest/server";
	private final String CONTAINER = "work-flow";
	private final String PROCESS_ID = "work-flow.work-assignment";
	
	private final boolean BYPASS_AUTH = true;

	public static void main(String[] args) {
		KieClient clientApp = new KieClient();
		
		LOGGER.info("Test 1 begin ***");
		clientApp.executeTest1();
		LOGGER.info("Test 1 End ***");
	}
	

	private void executeTest1() {
		String userId = "UserLC";
		String password = "Pa$$w0rd";
		
		KieFacade kieFacadeLC = KieFacade.getInstance(KIE_SERVER_URL, userId, password, BYPASS_AUTH);
		Long piid = launchProcess(kieFacadeLC);
		LOGGER.info("Test 1 Process ID [" + piid + "]");
		
		findProcessesByProcessId(kieFacadeLC, piid);
		findProcessInstances(kieFacadeLC);
		tasksAssignedAsPotentialUser(kieFacadeLC, userId);
		getTasks(kieFacadeLC, userId);
	}
	
	public Long launchProcess(KieFacade kieFacade) {
		try {
			Map<String, Object> inputData = new HashMap<>();
			setInputData(inputData);
			return kieFacade.getProcessServicesClient().startProcess(CONTAINER, PROCESS_ID, inputData);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void findProcessInstances(KieFacade kieFacade) {
		List<ProcessInstance> pis = kieFacade.getProcessServicesClient().findProcessInstances(CONTAINER, 0, 10);
		LOGGER.info("findProcessInstances :" + pis);
	}
	
	public void findProcessesByProcessId(KieFacade kieFacade, final Long piid) {
		try {
			ProcessInstance pi = kieFacade.getQueryServicesClient().findProcessInstanceById(piid, true);
			LOGGER.info("pi Details:" + pi);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void tasksAssignedAsPotentialUser(KieFacade kieFacade, final String userId) {
		try {
			List<TaskSummary> tasks = kieFacade.getUserTaskServicesClient().findTasksAssignedAsPotentialOwner(userId, 0, 10);
			LOGGER.info("findTasksAssignedAsPotentialOwner" + tasks);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void getTasks(KieFacade kieFacade, String userId) {
		try {
			List<TaskSummary> tasks = kieFacade.getUserTaskServicesClient().findTasks(userId, 0, 10);
			LOGGER.info("findTasks" + tasks);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setInputData(Map<String, Object> inputData) {
		inputData.put("faceAmount", 50000);
		inputData.put("age", 35);
	}
}
