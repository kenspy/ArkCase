/**
 * TaskWizard.Callback
 *
 * Callback handlers for server responses
 *
 * @author jwu
 */
TaskWizard.Callback = {
    create : function() {
        Acm.Dispatcher.addEventListener(this.EVENT_ASSIGNEES_RETRIEVED, this.onAssigneesRetrieved);
        Acm.Dispatcher.addEventListener(this.EVENT_TASK_CREATED, this.onTaskCreated);
    }

    ,EVENT_ASSIGNEES_RETRIEVED  : "task-wizard-get-assignees"
    ,EVENT_TASK_CREATED		    : "task-wizard-task-created"

    ,onAssigneesRetrieved : function(Callback, response) {
        var success = false;
        if (response) {
            TaskWizard.setAssignees(response.response.docs);
            success = true;
        }

        if (!success) {
            Acm.Dialog.error("Failed to retrieve assignees");
        }
    }
    ,onTaskCreated : function(Callback, response) {
        if (response.hasError) {
            Acm.Dialog.error("Failed to create new task:"  +response.errorMsg);
        } else {
            if (Acm.isNotEmpty(response.taskId)) {
                TaskWizard.Object.setTaskData(response);

                var attachedToObjectType = response.attachedToObjectType;
                var attachedToObjectId = response.attachedToObjectId;
                var url;
                if (Acm.isEmpty(attachedToObjectId)) {
                    url = TaskWizard.Page.URL_TASK;
                } else {
                    if ("COMPLAINT" == attachedToObjectType) {
                        url = TaskWizard.Page.URL_PARENT_COMPLAINT;
                    }
                    else if ("CASE_FILE" == attachedToObjectType){
                        url = TaskWizard.Page.URL_PARENT_CASE;
                    }
                    url += attachedToObjectId;
                }
                App.gotoPage(url);
            }
        }
    }
};
