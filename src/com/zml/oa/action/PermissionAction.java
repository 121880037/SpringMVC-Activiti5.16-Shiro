package com.zml.oa.action;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.zml.oa.entity.UserTask;
import com.zml.oa.pagination.Pagination;
import com.zml.oa.pagination.PaginationThreadUtils;
import com.zml.oa.service.IUserTaskService;

/**
 * @ClassName: PermissionAction
 * @Description:权限管理
 * @author: zml
 * @date: 2015-03-09 上午12:19:10
 *
 */

@Controller
@RequiresPermissions("admin:*")
@RequestMapping("/permissionAction")
public class PermissionAction {

    @Autowired
    protected RepositoryService repositoryService;
    
    @Autowired
    protected IUserTaskService userTaskService;
	
    @RequestMapping("/loadBpmn_page")
	public String loadBpmnInfo(Model model){
		List<Object> objects = new ArrayList<Object>();
		ProcessDefinitionQuery proDefQuery = repositoryService.createProcessDefinitionQuery().orderByDeploymentId().desc();
		Integer totalSum = proDefQuery.list().size();
		int[] pageParams = PaginationThreadUtils.setPage(totalSum);
		Pagination pagination = PaginationThreadUtils.get();
		List<ProcessDefinition> processDefinitionList = proDefQuery.listPage(pageParams[0], pageParams[1]);
		model.addAttribute("proDefList", processDefinitionList);
		model.addAttribute("page", pagination.getPageStr());
		return "permission/list_bpmn";
	}
	
	@RequestMapping("/setAuthor")
	public String setAuthor(@RequestParam("id") String processDefinitionId) {
		ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) repositoryService.getProcessDefinition(processDefinitionId);
		List<ActivityImpl> activitiList = processDefinition.getActivities();//获得当前任务的所有节点
		for (ActivityImpl activity : activitiList) {
			
		}
		
		return null;
	}
	
	@RequestMapping("/initialization")
	public String initialization(RedirectAttributes redirectAttributes) throws Exception {
		ProcessDefinitionQuery proDefQuery = repositoryService.createProcessDefinitionQuery().orderByDeploymentId().desc();
		List<ProcessDefinition> processDefinitionList = proDefQuery.list();
		for(ProcessDefinition processDefinition : processDefinitionList){
			ProcessDefinitionEntity processDef = (ProcessDefinitionEntity) repositoryService.getProcessDefinition(processDefinition.getId());
			List<ActivityImpl> activitiList = processDef.getActivities();//获得当前任务的所有节点
			for (ActivityImpl activity : activitiList) {
				UserTask userTask = new UserTask();
				userTask.setProcDefId(processDefinition.getId());
				userTask.setProcDefKey(processDefinition.getKey());
				userTask.setProcDefName(processDefinition.getName());
				ActivityBehavior activityBehavior = activity.getActivityBehavior();
				//是否为用户任务
				if (activityBehavior instanceof UserTaskActivityBehavior) {
					UserTaskActivityBehavior userTaskActivityBehavior = (UserTaskActivityBehavior) activityBehavior;
		            TaskDefinition taskDefinition = userTaskActivityBehavior.getTaskDefinition();
		            
		            //任务所属角色
		            String taskDefKey = taskDefinition.getKey();
		            Expression taskName = taskDefinition.getNameExpression();
		            System.out.println("taskDefKey: "+taskDefKey+"-------- :"+taskDefinition.getNameExpression());
		            userTask.setTaskDefKey(taskDefKey);
		            userTask.setTaskName(taskName.toString());
		            this.userTaskService.doAdd(userTask);
				}
				
			}
		}
		redirectAttributes.addAttribute("message", "初始化成功！");
		return "redirect:/permissionAction/loadBpmn_page";
	}
}
