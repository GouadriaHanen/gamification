package org.exoplatform.addons.gamification.listener.task;

import static org.exoplatform.addons.gamification.GamificationConstant.*;

import org.exoplatform.addons.gamification.entities.domain.effective.GamificationActionsHistory;
import org.exoplatform.addons.gamification.service.configuration.RuleService;
import org.exoplatform.addons.gamification.service.dto.configuration.RuleDTO;
import org.exoplatform.addons.gamification.service.effective.GamificationService;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.service.TaskPayload;
import org.exoplatform.task.service.TaskService;
import org.exoplatform.task.util.TaskUtil;

import java.util.Iterator;
import java.util.Set;

public class GamificationTaskUpdateListener extends Listener<TaskService, TaskPayload> {
  private static final Log LOG = ExoLogger.getLogger(GamificationTaskUpdateListener.class);

    protected RuleService ruleService;
    protected IdentityManager identityManager;
    protected GamificationService gamificationService;

    public GamificationTaskUpdateListener(RuleService ruleService, IdentityManager identityManager, GamificationService gamificationService) {
        this.ruleService = ruleService;
        this.identityManager = identityManager;
        this.gamificationService = gamificationService;
    }


    @Override
    public void onEvent(Event<TaskService, TaskPayload> event) throws Exception {

        TaskPayload data = event.getData();

        Task oldTask = data.before();
        Task newTask = data.after();

        // New Task has been created
        if (oldTask == null && newTask != null) {
            createTask(newTask);
        }

        // Update Task
        if (oldTask != null && newTask != null) {
            updateTask(oldTask, newTask);
        }

    }

    protected void createTask(Task task) {

        String actorUsername = ConversationState.getCurrent().getIdentity().getUserId();

        // Compute user id
        String actorId = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, actorUsername, false).getId();

        gamificationService.createHistory(GAMIFICATION_TASK_ADDON_CREATE_TASK, actorId,actorId,TaskUtil.buildTaskURL(task));

    }

    protected void updateTask(Task before, Task after) {
        RuleDTO ruleDto = null;
        String actorId = "";
        GamificationActionsHistory aHistory = null;
        // Task completed
        if (isDiff(before.isCompleted(), after.isCompleted())) {


            // Get task assigned property
            if (after.getAssignee() != null && after.getAssignee().length() != 0) {
                // Compute user id
                actorId = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, after.getAssignee(), false).getId();

                gamificationService.createHistory(GAMIFICATION_TASK_ADDON_COMPLETED_TASK_ASSIGNED, actorId,actorId, TaskUtil.buildTaskURL(after));

            }

            // Manage coworker
            Set<String> cowrokers = after.getCoworker();
            // Get task assigned property
            if (cowrokers == null) return;

            Iterator<String> coworker = cowrokers.iterator();
            while (coworker.hasNext()) {
                // Compute user id
                actorId = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, coworker.next(), false).getId();
                gamificationService.createHistory(GAMIFICATION_TASK_ADDON_COMPLETED_TASK_COWORKER, actorId,actorId,TaskUtil.buildTaskURL(after));
            }
        } else { // Update a task regardless le action made by a user

            // Get connected user
            String actorUsername = ConversationState.getCurrent().getIdentity().getUserId();

            // Compute user id
            actorId = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, actorUsername, false).getId();

            gamificationService.createHistory(GAMIFICATION_TASK_ADDON_UPDATE_TASK, actorId,actorId,TaskUtil.buildTaskURL(after));

        }
    }

    /**
     * Check whether a task has been updated
     *
     * @param before : Task data before modification
     * @param after  : Task data after modification
     * @return a boolean if task has been changed false else
     */
    protected boolean isDiff(Object before, Object after) {
        if (before == after) {
            return false;
        }
        if (before != null) {
            return !before.equals(after);
        } else {
            return !after.equals(before);
        }
    }
}
