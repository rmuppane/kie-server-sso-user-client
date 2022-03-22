package com.rh.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kie.server.api.model.definition.QueryDefinition;
import org.kie.server.api.model.definition.TaskField;
import org.kie.server.api.model.definition.TaskQueryFilterSpec;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.api.util.TaskQueryFilterSpecBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KieClient {

	final static Logger LOGGER = LoggerFactory.getLogger(KieClient.class);

	private final String KIE_SERVER_URL = "http://localhost:8090/rest/server";
	private final String CONTAINER = "work-flow";
	private final String PROCESS_ID = "work-flow.work-assignment";
	
	private final boolean BYPASS_AUTH = false;

	public static void main(String[] args) {
		KieClient clientApp = new KieClient();
		
		/*LOGGER.info("Test 1 begin ***");
		clientApp.executeTest1();
		LOGGER.info("Test 1 End ***");*/
		
		LOGGER.info("Test 2 begin ***");
		clientApp.executeTest2();
		LOGGER.info("Test 2 End ***");
	}
	

	private void executeTest1() {
		String teamleader_lc = "teamlead_lc";
		String password = "Pa$$w0rd";
		
		KieFacade kieFacadeTeamLeadLC = KieFacade.getInstance(KIE_SERVER_URL, teamleader_lc, password, BYPASS_AUTH);
		Long piid = launchProcess(kieFacadeTeamLeadLC);
		LOGGER.info("Test 1 Process ID [" + piid + "]");
		
		findProcessesByProcessId(kieFacadeTeamLeadLC, piid);
		findProcessInstances(kieFacadeTeamLeadLC);
		List<TaskSummary> tasks = tasksAssignedAsPotentialUser(kieFacadeTeamLeadLC, teamleader_lc);
		getTasks(kieFacadeTeamLeadLC, teamleader_lc);
		claimTheTask(kieFacadeTeamLeadLC, tasks.get(0).getId(), teamleader_lc);
		getTasks(kieFacadeTeamLeadLC, teamleader_lc);
		startTheTask(kieFacadeTeamLeadLC, tasks.get(0).getId(), teamleader_lc);
		completeTheTask(kieFacadeTeamLeadLC, tasks.get(0).getId(), teamleader_lc);
		findProcessInstances(kieFacadeTeamLeadLC);
		tasksAssignedAsPotentialUser(kieFacadeTeamLeadLC, teamleader_lc);
	}
	
	private void executeTest2() { 
		String teamleader_lc = "teamlead_lc";
		String password = "Pa$$w0rd";
		
		String technician_l1 = "technician_l1";
		
		KieFacade kieFacadeTeamLeadLC = KieFacade.getInstance(KIE_SERVER_URL, teamleader_lc, password, BYPASS_AUTH);
		KieFacade kieFacadeTechnicianL1 = KieFacade.getInstance(KIE_SERVER_URL, technician_l1, password, BYPASS_AUTH);
		
		Long piid = launchProcess(kieFacadeTeamLeadLC);
		LOGGER.info("Test 2 Process ID [" + piid + "]");
		
		findProcessesByProcessId(kieFacadeTeamLeadLC, piid);
		findProcessInstances(kieFacadeTeamLeadLC);
		
		List<TaskSummary> tasks = tasksAssignedAsPotentialUser(kieFacadeTeamLeadLC, teamleader_lc); // For LC
		tasksAssignedAsPotentialUser(kieFacadeTechnicianL1, technician_l1); // For L
		
		getTasks(kieFacadeTeamLeadLC, teamleader_lc);
		getTasks(kieFacadeTechnicianL1, technician_l1);
		
		delegateTheTask(kieFacadeTeamLeadLC, tasks.get(0).getId(), teamleader_lc, technician_l1);
		
		getTasks(kieFacadeTeamLeadLC, teamleader_lc); // For LC
		getTasks(kieFacadeTechnicianL1, technician_l1); // For L
		
		getOtherUsertasks(kieFacadeTeamLeadLC, teamleader_lc, technician_l1);
		
		startTheTask(kieFacadeTechnicianL1, tasks.get(0).getId(), technician_l1);
		completeTheTask(kieFacadeTechnicianL1, tasks.get(0).getId(), technician_l1);
		
		findProcessInstances(kieFacadeTeamLeadLC);
		
		tasksAssignedAsPotentialUser(kieFacadeTeamLeadLC, teamleader_lc); // For LC
		
		
		close(kieFacadeTeamLeadLC);
		close(kieFacadeTechnicianL1);
	}

	private void getOtherUsertasks(KieFacade kieFacade, String loggedInUser, String targetUser) {
		final String QUERY_NAME = "taskInstancesQuery";
	    final String TASK_QUERY = "select ti.* from AuditTaskImpl ti";
	    
	    QueryDefinition query = new QueryDefinition();
        query.setName(QUERY_NAME);
        query.setSource(System.getProperty("org.kie.server.persistence.ds", "jdbc/jbpm-ds"));
        query.setExpression(TASK_QUERY);
        query.setTarget("CUSTOM");
        kieFacade.getQueryServicesClient().registerQuery(query);
	    
        HashMap<TaskField, String> compareList = new HashMap<>();
        
        compareList.put( TaskField.ACTUALOWNER,
        		targetUser );
        
        List<TaskInstance> results = kieFacade.getQueryServicesClient().findHumanTasksWithFilters( QUERY_NAME, 
                createQueryFilterAndEqualsTo( compareList ), 0, 100 );
        
        LOGGER.info("Other User Tasks :" + results);
        
        kieFacade.getQueryServicesClient().unregisterQuery(QUERY_NAME);
        
	}
	
	private void close(KieFacade kieFacade) {
		kieFacade.close();
	}


	private TaskQueryFilterSpec createQueryFilterAndEqualsTo( Map<TaskField, String> filterProperties ) {
		TaskQueryFilterSpecBuilder result = new TaskQueryFilterSpecBuilder();
		filterProperties.forEach( result::equalsTo );
		return result.get();
	}

	private void delegateTheTask(KieFacade kieFacade, long taskId, String user, String target_User) {
		kieFacade.getUserTaskServicesClient().delegateTask(CONTAINER, taskId, user, target_User);
	}


	private void completeTheTask(KieFacade kieFacade, Long taskId, String user) {
		Map<String, Object> map = new HashMap<String, Object>();
		kieFacade.getUserTaskServicesClient().completeTask(CONTAINER, taskId, user, map);
		
	}


	private void startTheTask(KieFacade kieFacade, Long taskId, String user) {
		kieFacade.getUserTaskServicesClient().startTask(CONTAINER, taskId, user);
	}


	private void claimTheTask(KieFacade kieFacade, Long taskId, String user) {
		kieFacade.getUserTaskServicesClient().claimTask(CONTAINER, taskId, user);
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
	
	public List<TaskSummary>  tasksAssignedAsPotentialUser(KieFacade kieFacade, final String userId) {
		List<TaskSummary> tasks = null;
		try {
			tasks = kieFacade.getUserTaskServicesClient().findTasksAssignedAsPotentialOwner(userId, 0, 10);
			LOGGER.info("findTasksAssignedAsPotentialOwner" + tasks);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tasks;
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
