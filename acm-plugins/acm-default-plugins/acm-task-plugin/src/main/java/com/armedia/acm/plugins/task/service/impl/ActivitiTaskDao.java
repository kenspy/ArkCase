package com.armedia.acm.plugins.task.service.impl;


import com.armedia.acm.plugins.task.exception.AcmTaskException;
import com.armedia.acm.plugins.task.model.AcmTask;
import com.armedia.acm.plugins.task.model.NumberOfDays;
import com.armedia.acm.plugins.task.model.TaskConstants;
import com.armedia.acm.plugins.task.model.TaskOutcome;
import com.armedia.acm.plugins.task.model.WorkflowHistoryInstance;
import com.armedia.acm.plugins.task.service.TaskDao;
import com.armedia.acm.services.dataaccess.service.impl.DataAccessPrivilegeListener;
import com.armedia.acm.services.participants.dao.AcmParticipantDao;
import com.armedia.acm.services.participants.model.AcmParticipant;
import com.armedia.acm.services.users.dao.ldap.UserDao;
import com.armedia.acm.services.users.model.AcmUser;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FormProperty;
import org.activiti.bpmn.model.FormValue;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Task;
import org.apache.commons.lang.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;


class ActivitiTaskDao implements TaskDao
{
    private TaskService activitiTaskService;
    private RepositoryService activitiRepositoryService;
    private Logger log = LoggerFactory.getLogger(getClass());
    private HistoryService activitiHistoryService;
    private Map<String, Integer> priorityLevelToNumberMap;
    private Map<String, List<String>> requiredFieldsPerOutcomeMap;
    private UserDao userDao;
    private AcmParticipantDao participantDao;
    private DataAccessPrivilegeListener dataAccessPrivilegeListener;



    @Override
    @Transactional
    public AcmTask createAdHocTask(AcmTask in) throws AcmTaskException
    {
        Task activitiTask = getActivitiTaskService().newTask();

        return updateExistingActivitiTask(in, activitiTask);
    }

    @Override
    @Transactional
    public AcmTask save(AcmTask in) throws AcmTaskException
    {
        Task activitiTask = getActivitiTaskService().createTaskQuery().taskId(in.getTaskId().toString()).singleResult();
        if ( activitiTask != null )
        {
            return updateExistingActivitiTask(in, activitiTask);
        }

        // task must have been completed.  Try finding the historic task; but historical tasks can't be updated, so
        // even if we find it we have to throw an exception
        {
            HistoricTaskInstance hti = getActivitiHistoryService().
                    createHistoricTaskInstanceQuery().
                    taskId(in.getTaskId().toString()).
                    singleResult();

            if ( hti == null )
            {
                // no such task!
                throw new AcmTaskException("No such task with id '" + in.getTaskId() + "'");
            }
            else
            {
                throw new AcmTaskException("Task with id '" + in.getTaskId() + "' has already been completed and so " +
                        "it cannot be updated.");
            }
        }


    }

    private AcmTask updateExistingActivitiTask(AcmTask in, Task activitiTask) throws AcmTaskException
    {
        activitiTask.setAssignee(in.getAssignee());
        activitiTask.setOwner(in.getOwner());
        Integer activitiPriority = activitiPriorityFromAcmPriority(in.getPriority());
        activitiTask.setPriority(activitiPriority);
        activitiTask.setDueDate(in.getDueDate());
        activitiTask.setName(in.getTitle());

        try
        {
            getActivitiTaskService().saveTask(activitiTask);
            getActivitiTaskService().setVariableLocal(activitiTask.getId(), "OBJECT_TYPE", in.getAttachedToObjectType());
            getActivitiTaskService().setVariableLocal(activitiTask.getId(), "OBJECT_ID", in.getAttachedToObjectId());
            if ( in.getAttachedToObjectType() != null )
            {
                getActivitiTaskService().setVariableLocal(activitiTask.getId(), in.getAttachedToObjectType(), in.getAttachedToObjectId());
            }
            getActivitiTaskService().setVariableLocal(activitiTask.getId(), "OBJECT_NAME", in.getAttachedToObjectName());
            getActivitiTaskService().setVariableLocal(activitiTask.getId(), "START_DATE", in.getTaskStartDate());
            getActivitiTaskService().setVariableLocal(activitiTask.getId(), "PERCENT_COMPLETE", in.getPercentComplete());
            getActivitiTaskService().setVariableLocal(activitiTask.getId(), "DETAILS", in.getDetails());

            getActivitiTaskService().setVariable(activitiTask.getId(), "REWORK_INSTRUCTIONS", in.getReworkInstructions());

            if ( in.getTaskOutcome() != null )
            {
                getActivitiTaskService().setVariableLocal(activitiTask.getId(), "outcome", in.getTaskOutcome().getName());
            }

            in.setTaskId(Long.valueOf(activitiTask.getId()));
            in.setCreateDate(activitiTask.getCreateTime());

            // make sure an assignee participant is there, so the right data access can be set on the assignee...
            // activiti has to control the assignee, not the assignment rules.
            ensureCorrectAssigneeInParticipants(in);

            // to ensure that the participants for new and updated ad-hoc tasks are visible to the client right away, we
            // have to apply the assignment and data access control rules right here, inline with the save operation.
            // Tasks generated or updated by the Activiti engine will have participants set by a specialized
            // Mule flow.
            getDataAccessPrivilegeListener().applyAssignmentAndAccessRules(in);

            // Now we have to check the assignee again, to be sure the Activiti task assignee is the "assignee"
            // participant.  I know we're calling the same method twice!, to overwrite any changes the rules make to the
            // assignee... In short, Activiti controls the task assignee, not the assignment rules.
            ensureCorrectAssigneeInParticipants(in);   // there's a good reason we call this again, see above

            // the rules (or the user) may have removed some participants.  We want to delete all participants other
            // than the ones we just now validated.
            getParticipantDao().removeAllOtherParticipantsForObject("TASK", in.getTaskId(), in.getParticipants());
            in.setParticipants(getParticipantDao().saveParticipants(in.getParticipants()));

            return in;
        }
        catch (ActivitiException e)
        {
            throw new AcmTaskException(e.getMessage(), e);
        }
    }

    public void ensureCorrectAssigneeInParticipants(AcmTask in)
    {
        boolean assigneeFound = false;

        if ( in.getParticipants() != null )
        {
            for ( AcmParticipant ap : in.getParticipants() )
            {
                if ( "assignee".equals(ap.getParticipantType()) )
                {
                    if ( in.getAssignee() == null )
                    {
                        // task has no assignee so we need to remove this participant
                        in.getParticipants().remove(ap);
                    }
                    else
                    {
                        assigneeFound = true;
                        if ( ap.getParticipantLdapId() == null || !ap.getParticipantLdapId().equalsIgnoreCase(in.getAssignee()) )
                        {
                            ap.setParticipantLdapId(in.getAssignee());
                            break;
                        }
                    }

                }
            }
        }

        if ( ! assigneeFound && in.getAssignee() != null )
        {
            AcmParticipant assignee = new AcmParticipant();
            assignee.setParticipantLdapId(in.getAssignee());
            assignee.setParticipantType("assignee");
            assignee.setObjectId(in.getTaskId());
            assignee.setObjectType("TASK");

            if ( in.getParticipants() == null )
            {
                in.setParticipants(new ArrayList<AcmParticipant>());
            }
            in.getParticipants().add(assignee);
        }
    }

    @Override
    @Transactional
    public AcmTask completeTask(Principal userThatCompletedTheTask, Long taskId) throws AcmTaskException
    {
        return completeTask(userThatCompletedTheTask, taskId, null, null);
    }

    @Override
    public AcmTask completeTask(Principal userThatCompletedTheTask, Long taskId, String outcomePropertyName, String outcomeId)
            throws AcmTaskException
    {

        verifyCompleteTaskArgs(userThatCompletedTheTask, taskId);

        String user = userThatCompletedTheTask.getName();

        if ( log.isInfoEnabled() )
        {
            log.info("Completing task '" + taskId + "' for user '" + user + "'");
        }

        String strTaskId = String.valueOf(taskId);

        Task existingTask = getActivitiTaskService().
                createTaskQuery().
                includeProcessVariables().
                includeTaskLocalVariables().
                taskId(strTaskId).
                singleResult();

        verifyTaskExists(taskId, existingTask);

        verifyUserIsTheAssignee(taskId, user, existingTask);

        AcmTask retval = acmTaskFromActivitiTask(existingTask);

        try
        {
            if ( outcomePropertyName != null && outcomeId != null )
            {
                getActivitiTaskService().setVariable(strTaskId, outcomePropertyName, outcomeId);
                getActivitiTaskService().setVariableLocal(strTaskId, "outcome", outcomeId);
            }

            getActivitiTaskService().complete(strTaskId);

            HistoricTaskInstance hti =
                    getActivitiHistoryService().createHistoricTaskInstanceQuery().taskId(strTaskId).singleResult();

            retval.setTaskStartDate(hti.getStartTime());
            retval.setTaskFinishedDate(hti.getEndTime());
            retval.setTaskDurationInMillis(hti.getDurationInMillis());
            retval.setCompleted(true);

            return retval;
        }
        catch (ActivitiException e)
        {
            log.error("Could not close task '" + taskId + "' for user '" + user + "': " + e.getMessage(), e);
            throw new AcmTaskException(e);
        }
    }


    @Override
    @Transactional
    public AcmTask deleteTask(Principal userThatCompletedTheTask, Long taskId) throws AcmTaskException
    {
        verifyCompleteTaskArgs(userThatCompletedTheTask, taskId);

        String user = userThatCompletedTheTask.getName();

        if ( log.isInfoEnabled() )
        {
            log.info("Deleting task '" + taskId + "' for user '" + user + "'");
        }

        String strTaskId = String.valueOf(taskId);

        Task existingTask = getActivitiTaskService().
                createTaskQuery().
                includeProcessVariables().
                includeTaskLocalVariables().
                taskId(strTaskId).
                singleResult();

        verifyTaskExists(taskId, existingTask);

        verifyUserIsTheAssignee(taskId, user, existingTask);

        AcmTask retval = acmTaskFromActivitiTask(existingTask);

        try
        {
            getActivitiTaskService().deleteTask(strTaskId);

            HistoricTaskInstance hti =
                    getActivitiHistoryService().createHistoricTaskInstanceQuery().taskId(strTaskId).singleResult();

            retval.setTaskStartDate(hti.getStartTime());
            retval.setTaskFinishedDate(hti.getEndTime());
            retval.setTaskDurationInMillis(hti.getDurationInMillis());
            retval.setCompleted(true);

            return retval;
        }
        catch (ActivitiException e)
        {
            log.error("Could not close task '" + taskId + "' for user '" + user + "': " + e.getMessage(), e);
            throw new AcmTaskException(e);
        }
    }

    protected void verifyCompleteTaskArgs(Principal userThatCompletedTheTask, Long taskId)
    {
        if ( userThatCompletedTheTask == null || taskId == null )
        {
            throw new IllegalArgumentException("userThatCompletedTheTask and taskId must be specified");
        }
    }

    protected void verifyUserIsTheAssignee(Long taskId, String user, Task existingTask) throws AcmTaskException
    {
        if ( existingTask.getAssignee() == null || !existingTask.getAssignee().equals(user))
        {
            throw new AcmTaskException("Task '" + taskId + "' can only be closed by the assignee.");
        }
    }

    protected void verifyTaskExists(Long taskId, Task existingTask) throws AcmTaskException
    {
        if ( existingTask == null )
        {
            throw new AcmTaskException("Task '" + taskId + "' does not exist or has already been completed.");
        }
    }

    @Override
    public List<AcmTask> tasksForUser(String user)
    {
        if ( log.isInfoEnabled() )
        {
            log.info("Finding all tasks for user '" + user + "'");
        }

        List<AcmTask> retval = new ArrayList<>();

        List<Task> activitiTasks = getActivitiTaskService().
                createTaskQuery().
                taskAssignee(user).
                includeProcessVariables().
                includeTaskLocalVariables().
                orderByDueDate().desc().
                list();

        if ( activitiTasks != null )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug("Found '" + activitiTasks.size() + "' tasks for user '" + user + "'");
            }

            for ( Task activitiTask : activitiTasks )
            {
                AcmTask acmTask = acmTaskFromActivitiTask(activitiTask);

                retval.add(acmTask);
            }
        }



        return retval;
    }
    @Override
    public List<AcmTask> allTasks() {
        if ( log.isInfoEnabled() )
        {
            log.info("Finding all tasks for all users '");
        }

        List<AcmTask> retval = new ArrayList<>();

        List<Task> activitiTasks = getActivitiTaskService().
                createTaskQuery().
                includeProcessVariables().
                includeTaskLocalVariables().
                orderByDueDate().desc().
                list();

        if ( activitiTasks != null )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug("Found '" + activitiTasks.size() + "' tasks for all users");
            }

            for ( Task activitiTask : activitiTasks )
            {
                AcmTask acmTask = acmTaskFromActivitiTask(activitiTask);

                retval.add(acmTask);
            }
        }

        return retval;
    }

    @Override
    public List<AcmTask> pastDueTasks() {
        if ( log.isInfoEnabled() )
        {
            log.info("Finding all tasks for all users that due date was before today");
        }

        List<AcmTask> retval = new ArrayList<>();

        List<Task> activitiTasks = getActivitiTaskService().
                createTaskQuery().
                includeProcessVariables().
                includeTaskLocalVariables().
                dueBefore(new Date()).
                list();

        if ( activitiTasks != null )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug("Found '" + activitiTasks.size() + "' tasks for all users with past due date");
            }

            for ( Task activitiTask : activitiTasks )
            {
                AcmTask acmTask = acmTaskFromActivitiTask(activitiTask);

                retval.add(acmTask);
            }
        }

        return retval;
    }

    @Override
    public List<AcmTask> dueSpecificDateTasks(NumberOfDays numberOfDaysFromToday) {
        if (log.isInfoEnabled())
        {
            log.info(String.format("Finding all tasks for all users which due date is until %s from today", numberOfDaysFromToday.getnDays()));
        }

        List<AcmTask> retval = new ArrayList<>();

        List<Task> activitiTasks = getActivitiTaskService().
                createTaskQuery().
                includeProcessVariables().
                includeTaskLocalVariables().
                dueAfter(new Date()).
                dueBefore(shiftDateFromToday(numberOfDaysFromToday.getNumOfDays())).
                list();

        if ( activitiTasks != null )
        {
            if (log.isDebugEnabled())
            {
                log.debug("Found '" + activitiTasks.size() + "' tasks for all users which due date is between today and " + numberOfDaysFromToday.getnDays() + " from today");
            }

            for (Task activitiTask : activitiTasks)
            {
                AcmTask acmTask = acmTaskFromActivitiTask(activitiTask);
                retval.add(acmTask);
            }
        }

        return retval;
    }

    @Override
    public AcmTask findById(Long taskId) throws AcmTaskException
    {
        if ( log.isInfoEnabled() )
        {
            log.info("Finding task with ID '" + taskId + "'");
        }
        AcmTask retval;

        Task activitiTask = getActivitiTaskService().
                createTaskQuery().
                taskId(String.valueOf(taskId)).
                includeProcessVariables().
                includeTaskLocalVariables().
                singleResult();
        if ( activitiTask != null )
        {
            retval = acmTaskFromActivitiTask(activitiTask);
            return retval;
        }
        else
        {
            HistoricTaskInstance hti = getActivitiHistoryService().
                    createHistoricTaskInstanceQuery().
                    taskId(String.valueOf(taskId)).
                    includeProcessVariables().
                    includeTaskLocalVariables().
                    singleResult();

            if ( hti != null )
            {
                retval = acmTaskFromHistoricActivitiTask(hti);

                return retval;
            }
        }

        throw new AcmTaskException("Task with ID '" + taskId + "' does not exist.");

    }
    
    @Override
	public List<WorkflowHistoryInstance> getWorkflowHistory(String id, boolean adhoc) {
    	
    	List<WorkflowHistoryInstance> retval = new ArrayList<>();
    	
    	HistoricTaskInstanceQuery query;

        // due to an Activiti issue, we have to retrieve the task local issues separately for each task instance.
        // if we ask for them at the query level (via "includeTaskLocalVariables") Activiti basically returns one big
        // map, instead of one map per historic task instance.  Obviously the one big map will contain correct values
        // only for the last task retrieved.
    	if (!adhoc)
    	{
    		query = getActivitiHistoryService().createHistoricTaskInstanceQuery().processInstanceId(id).includeTaskLocalVariables().orderByHistoricTaskInstanceEndTime().asc();
    	}
    	else {
    		query = getActivitiHistoryService().createHistoricTaskInstanceQuery().taskId(id).includeTaskLocalVariables().orderByHistoricTaskInstanceEndTime().asc();
    	}
    	
    	if (null != query)
    	{	    	
	    	List<HistoricTaskInstance> historicTaskInstances = query.list();
	    	
	    	if ( null != historicTaskInstances && !historicTaskInstances.isEmpty() )
	    	{
	    		for (HistoricTaskInstance historicTaskInstance : historicTaskInstances)
	    		{
	    			
	    			String taskId = historicTaskInstance.getId();

	    			// TODO: For now Role is empty. This is agreed with Dave. Once we have that information, we should add it here.
	    			String role = "";
	    			Date startDate = historicTaskInstance.getStartTime();
	    			Date endDate = historicTaskInstance.getEndTime();

                    String status = findTaskStatus(historicTaskInstance);

                    // for purposes of the workflow history, the task outcome should override the status.
                    Map<String, Object> localVariables = historicTaskInstance.getTaskLocalVariables();

                    if (null != localVariables && localVariables.containsKey("outcome"))
                    {
                        String outcome = (String) localVariables.get("outcome");
                        status = WordUtils.capitalizeFully(outcome.replaceAll("_", " "));
                    }
	    			
	    			WorkflowHistoryInstance workflowHistoryInstance = new WorkflowHistoryInstance();
	    			
	    			workflowHistoryInstance.setId(taskId);

	    			workflowHistoryInstance.setRole(role);
	    			workflowHistoryInstance.setStatus(status);
	    			workflowHistoryInstance.setStartDate(startDate);
	    			workflowHistoryInstance.setEndDate(endDate);

                    if ( historicTaskInstance.getAssignee() != null )
                    {
                        AcmUser user = getUserDao().findByUserId(historicTaskInstance.getAssignee());
                        String participant = user.getFullName();
                        workflowHistoryInstance.setParticipant(participant);
                    }
	    			
	    			retval.add(workflowHistoryInstance);
	    		}
	    	}
    	}
    	
		return retval;
	}

    private String findTaskStatus(HistoricTaskInstance historicTaskInstance)
    {
        return historicTaskInstance.getEndTime() == null ? TaskConstants.STATE_ACTIVE : TaskConstants.STATE_CLOSED;
    }

    private String findTaskStatus(Task task)
    {
        // tasks in ACT_RU_TASK table (where Task objects come from) are active by definition
        return TaskConstants.STATE_ACTIVE;
    }

    @Override
    public List<AcmTask> getTasksModifiedSince(Date lastModified, int start, int pageSize)
    {
        List<AcmTask> retval = new ArrayList<>();

        List<HistoricTaskInstance> tasks = getActivitiHistoryService().
                createHistoricTaskInstanceQuery().
                includeProcessVariables().
                includeTaskLocalVariables().
                taskCreatedAfter(lastModified).
                orderByTaskId().
                asc().listPage(start, pageSize);

        if ( tasks != null )
        {
            for ( HistoricTaskInstance task : tasks )
            {
                AcmTask active = acmTaskFromHistoricActivitiTask(task);
                retval.add(active);
            }
        }

        return retval;

    }

    protected AcmTask acmTaskFromHistoricActivitiTask(HistoricTaskInstance hti)
    {
        if ( hti == null )
        {
            return null;
        }

        // even active tasks have an entry in the historic task table, so this HistoricTaskInstance may
        // represent an active task
        AcmTask retval;
        retval = new AcmTask();
        retval.setTaskStartDate(hti.getStartTime());
        retval.setTaskFinishedDate(hti.getEndTime());
        retval.setTaskDurationInMillis(hti.getDurationInMillis());
        retval.setCompleted(hti.getEndTime() != null);

        retval.setTaskId(Long.valueOf(hti.getId()));
        retval.setDueDate(hti.getDueDate());
        String taskPriority = acmPriorityFromActivitiPriority(hti.getPriority());
        retval.setPriority(taskPriority);
        retval.setTitle(hti.getName());
        retval.setAssignee(hti.getAssignee());

        if ( hti.getProcessVariables() != null )
        {
            retval.setAttachedToObjectId((Long) hti.getProcessVariables().get("OBJECT_ID"));
            retval.setAttachedToObjectType((String) hti.getProcessVariables().get("OBJECT_TYPE"));
            retval.setAttachedToObjectName((String) hti.getProcessVariables().get("OBJECT_NAME"));
            retval.setWorkflowRequestId((Long) hti.getProcessVariables().get("REQUEST_ID"));
            retval.setWorkflowRequestType((String) hti.getProcessVariables().get("REQUEST_TYPE"));
            retval.setReviewDocumentPdfRenditionId((Long) hti.getProcessVariables().get("pdfRenditionId"));
            retval.setReviewDocumentFormXmlId((Long) hti.getProcessVariables().get("formXmlId"));
            retval.setReworkInstructions((String) hti.getProcessVariables().get("REWORK_INSTRUCTIONS"));

        }

        if ( hti.getTaskLocalVariables() != null )
        {
            Map<String, Object> taskLocal = hti.getTaskLocalVariables();

            extractTaskLocalVariables(retval, taskLocal);
        }

        String status = findTaskStatus(hti);
        retval.setStatus(status);

        String pid = hti.getProcessDefinitionId();
        String processInstanceId = hti.getProcessInstanceId();
        String taskDefinitionKey = hti.getTaskDefinitionKey();
        if ( pid != null )
        {
            findProcessNameAndTaskOutcomes(retval, pid, processInstanceId, taskDefinitionKey);
            findSelectedTaskOutcome(hti, retval);
        }
        else
        {
            retval.setAdhocTask(true);
        }

        List<AcmParticipant> participants = getParticipantDao().findParticipantsForObject("TASK", retval.getTaskId());
        retval.setParticipants(participants);

        if ( log.isTraceEnabled() )
        {
            log.trace("Activiti task id '" + retval.getTaskId() + "' for object type '" +
                    retval.getAttachedToObjectType() + "'" +
                    ", object id '" + retval.getAttachedToObjectId() + "' found for user '" + retval.getAssignee()
                    + "'");
        }

        return retval;
    }

    private void findSelectedTaskOutcome(HistoricTaskInstance hti, AcmTask retval)
    {
        // check for selected task outcome
        String outcomeId = (String) hti.getTaskLocalVariables().get("outcome");
        if ( outcomeId != null )
        {
            for ( TaskOutcome availableOutcome : retval.getAvailableOutcomes() )
            {
                if ( outcomeId.equals(availableOutcome.getName()) )
                {
                    retval.setTaskOutcome(availableOutcome);
                    break;
                }
            }
        }
    }

    private void findProcessNameAndTaskOutcomes(
            AcmTask retval,
            String processDefinitionId,
            String processInstanceId,
            String taskDefinitionKey)
    {
        ProcessDefinition pd =
                getActivitiRepositoryService().createProcessDefinitionQuery().processDefinitionId(processDefinitionId).singleResult();
        retval.setBusinessProcessName(pd.getName());
        retval.setAdhocTask(false);
        retval.setBusinessProcessId(
                processInstanceId == null ? null : Long.valueOf(processInstanceId));

        List<FormProperty> formProperties = findFormPropertiesForTask(processDefinitionId, taskDefinitionKey);
        if ( formProperties != null )
        {
            for ( FormProperty fp : formProperties )
            {
                log.debug("form property name: " + fp.getName() + "; id: " + fp.getId());
                if ( fp.getId() != null && fp.getId().endsWith("Outcome"))
                {
                    retval.setOutcomeName(fp.getId());
                    for ( FormValue fv : fp.getFormValues() )
                    {
                        log.debug(fv.getId() + " = " + fv.getName());
                        TaskOutcome outcome = new TaskOutcome();
                        outcome.setName(fv.getId());
                        outcome.setDescription(fv.getName());
                        outcome.setFieldsRequiredWhenOutcomeIsChosen(getRequiredFieldsPerOutcomeMap().get(fv.getId()));
                        retval.getAvailableOutcomes().add(outcome);
                    }
                }
            }
        }
    }

    private List<FormProperty> findFormPropertiesForTask(String processDefinitionId, String taskDefinitionKey)
    {
        BpmnModel model = getActivitiRepositoryService().getBpmnModel(processDefinitionId);

        List<Process> processes = model.getProcesses();

        Process p = processes.get(0);

        FlowElement taskFlowElement = p.getFlowElement(taskDefinitionKey);

        log.debug("task flow type: " + taskFlowElement.getClass().getName());

        UserTask ut = (UserTask) taskFlowElement;

        List<FormProperty> formProperties = ut.getFormProperties();

        return formProperties;

    }

    private void extractTaskLocalVariables(AcmTask acmTask, Map<String, Object> taskLocal)
    {
        if ( acmTask.getAttachedToObjectId() == null )
        {
            Long objectId = (Long) taskLocal.get("OBJECT_ID");
            acmTask.setAttachedToObjectId(objectId);
        }
        if ( acmTask.getAttachedToObjectType() == null )
        {
            String objectType = (String) taskLocal.get("OBJECT_TYPE");
            acmTask.setAttachedToObjectType(objectType);
        }
        if ( acmTask.getAttachedToObjectName() == null )
        {
            String objectName = (String) taskLocal.get("OBJECT_NAME");
            acmTask.setAttachedToObjectName(objectName);
        }
        Date startDate = (Date) taskLocal.get("START_DATE");
        acmTask.setTaskStartDate(startDate);

        Integer percentComplete = (Integer) taskLocal.get("PERCENT_COMPLETE");
        acmTask.setPercentComplete(percentComplete);

        String details = (String) taskLocal.get("DETAILS");
        acmTask.setDetails(details);
    }

    private String acmPriorityFromActivitiPriority(int priority)
    {
        String defaultPriority = "Medium";

        for ( Map.Entry<String, Integer> acmToActiviti : getPriorityLevelToNumberMap().entrySet() )
        {
            if ( acmToActiviti.getValue().equals(priority) )
            {
                return acmToActiviti.getKey();
            }
        }

        return defaultPriority;
    }

    private Integer activitiPriorityFromAcmPriority(String acmPriority)
    {
        for ( Map.Entry<String, Integer> acmToActiviti : getPriorityLevelToNumberMap().entrySet() )
        {
            if ( acmToActiviti.getKey().equals(acmPriority) )
            {
                return acmToActiviti.getValue();
            }
        }

        return TaskConstants.DEFAULT_PRIORITY;
    }

    protected AcmTask acmTaskFromActivitiTask(Task activitiTask)
    {
        if ( activitiTask == null )
        {
            return null;
        }

        AcmTask acmTask = new AcmTask();

        acmTask.setTaskId(Long.valueOf(activitiTask.getId()));
        acmTask.setDueDate(activitiTask.getDueDate());
        String taskPriority = acmPriorityFromActivitiPriority(activitiTask.getPriority());
        acmTask.setPriority(taskPriority);
        acmTask.setTitle(activitiTask.getName());
        acmTask.setAssignee(activitiTask.getAssignee());
        acmTask.setCreateDate(activitiTask.getCreateTime());
        acmTask.setOwner(activitiTask.getOwner());

        extractProcessVariables(activitiTask, acmTask);

        if ( activitiTask.getTaskLocalVariables() != null )
        {
            extractTaskLocalVariables(acmTask, activitiTask.getTaskLocalVariables());
        }

        String status = findTaskStatus(activitiTask);
        acmTask.setStatus(status);

        String pid = activitiTask.getProcessDefinitionId();
        String processInstanceId = activitiTask.getProcessInstanceId();
        String taskDefinitionKey = activitiTask.getTaskDefinitionKey();
        if ( pid != null )
        {
            findProcessNameAndTaskOutcomes(acmTask, pid, processInstanceId, taskDefinitionKey);
        }
        else
        {
            acmTask.setAdhocTask(true);
        }

        if ( log.isTraceEnabled() )
        {
            log.trace("Activiti task id '" + acmTask.getTaskId() + "' for object type '" +
                    acmTask.getAttachedToObjectType() + "'" +
                    ", object id '" + acmTask.getAttachedToObjectId() + ", object number '" + acmTask.getAttachedToObjectName() +"' found for user '" + acmTask.getAssignee()
                    + "'");
        }

        List<AcmParticipant> participants = getParticipantDao().findParticipantsForObject("TASK", acmTask.getTaskId());
        acmTask.setParticipants(participants);

        return acmTask;
    }



    private Date shiftDateFromToday(int daysFromToday){
        Date nextDate;
        Date today = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(today);
        cal.add(Calendar.DATE,daysFromToday);
        nextDate = cal.getTime();
        return nextDate;
    }

    protected void extractProcessVariables(Task activitiTask, AcmTask acmTask)
    {
        if ( activitiTask.getProcessVariables() != null )
        {
            acmTask.setAttachedToObjectId((Long) activitiTask.getProcessVariables().get("OBJECT_ID"));
            acmTask.setAttachedToObjectType((String) activitiTask.getProcessVariables().get("OBJECT_TYPE"));
            acmTask.setAttachedToObjectName((String) activitiTask.getProcessVariables().get("OBJECT_NAME"));
            acmTask.setWorkflowRequestId((Long) activitiTask.getProcessVariables().get("REQUEST_ID"));
            acmTask.setWorkflowRequestType((String) activitiTask.getProcessVariables().get("REQUEST_TYPE"));
            acmTask.setReviewDocumentPdfRenditionId((Long) activitiTask.getProcessVariables().get("pdfRenditionId"));
            acmTask.setReviewDocumentFormXmlId((Long) activitiTask.getProcessVariables().get("formXmlId"));
            acmTask.setReworkInstructions((String) activitiTask.getProcessVariables().get("REWORK_INSTRUCTIONS"));
        }
    }


    public TaskService getActivitiTaskService()
    {
        return activitiTaskService;
    }

    public void setActivitiTaskService(TaskService activitiTaskService)
    {
        this.activitiTaskService = activitiTaskService;
    }

    public RepositoryService getActivitiRepositoryService()
    {
        return activitiRepositoryService;
    }

    public void setActivitiRepositoryService(RepositoryService activitiRepositoryService)
    {
        this.activitiRepositoryService = activitiRepositoryService;
    }

    public void setActivitiHistoryService(HistoryService activitiHistoryService)
    {
        this.activitiHistoryService = activitiHistoryService;
    }

    public HistoryService getActivitiHistoryService()
    {
        return activitiHistoryService;
    }

    public Map<String, Integer> getPriorityLevelToNumberMap()
    {
        return priorityLevelToNumberMap;
    }

    public void setPriorityLevelToNumberMap(Map<String, Integer> priorityLevelToNumberMap)
    {
        this.priorityLevelToNumberMap = priorityLevelToNumberMap;
    }

    public Map<String, List<String>> getRequiredFieldsPerOutcomeMap()
    {
        return requiredFieldsPerOutcomeMap;
    }

    public void setRequiredFieldsPerOutcomeMap(Map<String, List<String>> requiredFieldsPerOutcomeMap)
    {
        this.requiredFieldsPerOutcomeMap = requiredFieldsPerOutcomeMap;
    }

	public UserDao getUserDao() 
	{
		return userDao;
	}

	public void setUserDao(UserDao userDao) 
	{
		this.userDao = userDao;
	}

    public AcmParticipantDao getParticipantDao()
    {
        return participantDao;
    }

    public void setParticipantDao(AcmParticipantDao participantDao)
    {
        this.participantDao = participantDao;
    }

    public DataAccessPrivilegeListener getDataAccessPrivilegeListener()
    {
        return dataAccessPrivilegeListener;
    }

    public void setDataAccessPrivilegeListener(DataAccessPrivilegeListener dataAccessPrivilegeListener)
    {
        this.dataAccessPrivilegeListener = dataAccessPrivilegeListener;
    }
}