package com.gwghk.mis.service;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.gwghk.mis.common.model.ApiResult;
import com.gwghk.mis.common.model.DetachedCriteria;
import com.gwghk.mis.common.model.Page;
import com.gwghk.mis.dao.ChatMessageDao;
import com.gwghk.mis.enums.ResultCode;
import com.gwghk.mis.model.ChatMessage;
import com.gwghk.mis.util.DateUtil;
import com.gwghk.mis.util.StringUtil;

/**
 * 聊天室信息管理服务类
 * @author Alan.wu
 * @date  2015年4月1日
 */
@Service
public class ChatMessgeService{

	@Autowired
	private ChatMessageDao chatMessageDao;

	@Autowired
	private ChatApiService chatApiService;
	/**
	 * 删除内容
	 * @param ids
	 * @return
	 */
	public ApiResult deleteChatMessage(String[] ids) {
		ApiResult api=new ApiResult();
		boolean isSuccess=chatMessageDao.deleteMessage(ids);
	    if(isSuccess){
	    	int size=0;
	    	String groupId="";
	    	ChatMessage row=null;
	    	List<ChatMessage> list=chatMessageDao.getListByIds(ids, "publishTime","groupId");
	    	if(list!=null && (size=list.size())>0){
	    		StringBuffer buffer=new StringBuffer();
	    		for(int i=0;i<size;i++){
	    			row=list.get(i);
	    			buffer.append(row.getPublishTime());
	    			groupId=row.getGroupId();
	    			if(i!=size-1){
	    				buffer.append(",");
	    			}
		    	}
	    		if(buffer.length()>0){
	    			//通知聊天室客户端移除对应记录
	        		chatApiService.removeMsg(buffer.toString(),groupId);
	    		}
	    	}
	    }
    	return api.setCode(isSuccess?ResultCode.OK:ResultCode.FAIL);
	}

    /**
     * 审批内容
     * @param publishTimeArr
     * @param fUserIdArr
     * @param status
     * @param groupId
     * @return
     */
    public ApiResult approvalMsg(String approvalUserNo,String publishTimeArr,String fUserIdArr,String status,String groupId){
    	return chatApiService.approvalMsg(approvalUserNo,publishTimeArr,fUserIdArr,status,groupId);
    }
	/**
	 * 分页查询内容
	 * @param dCriteria
	 * @return
	 */
	public Page<ChatMessage> getChatMessagePage(
			DetachedCriteria<ChatMessage> dCriteria) {
		Criteria criteria=new Criteria();
		ChatMessage model=dCriteria.getSearchModel();
		if(model!=null){
			if(StringUtils.isNotBlank(model.getMobilePhone())){
				criteria.and("mobilePhone").regex(StringUtil.toFuzzyMatch(model.getMobilePhone()));
			}
			if(StringUtils.isNotBlank(model.getAccountNo())){
				criteria.and("accountNo").regex(StringUtil.toFuzzyMatch(model.getAccountNo()));
			}
			if(model.getStatus()!=null){
				criteria.and("status").is(model.getStatus());
			}
			if(StringUtils.isNotBlank(model.getNickname())){
				criteria.and("nickname").regex(StringUtil.toFuzzyMatch(model.getNickname()));
			}
			if(StringUtils.isNotBlank(model.getClientGroup())){
				if(Pattern.compile("\\d{1}").matcher(model.getClientGroup()).matches()){
					criteria.and("userType").is(Integer.parseInt(model.getClientGroup()));
				}else{
					criteria.and("clientGroup").is(model.getClientGroup());
				}
			}
			if(StringUtils.isNotBlank(model.getGroupId())){
				if(model.getGroupId().indexOf(",")!=-1){
					criteria.and("groupType").is(model.getGroupId().replace(",", ""));
				}else{
					criteria.and("groupId").is(model.getGroupId());
				}
			}
			if(model.getContent()!=null){
				if(StringUtils.isNotBlank(model.getContent().getMsgType())){
					criteria.and("content.msgType").is(model.getContent().getMsgType());
				}
				if(StringUtils.isNotBlank(model.getContent().getValue())){
					criteria.and("content.msgType").is("text");
					criteria.and("content.value").regex(StringUtil.toFuzzyMatch(model.getContent().getValue()));
				}
			}
			if(model.getValid()!=null){
				criteria.and("valid").is(model.getValid());
			}
			if(StringUtils.isNotBlank(model.getPublishStartDateStr())){
				criteria = criteria.and("createDate").gte(DateUtil.parseDateSecondFormat(model.getPublishStartDateStr()));
			}
			if(StringUtils.isNotBlank(model.getPublishEndDateStr())){
				if(StringUtils.isNotBlank(model.getPublishStartDateStr())){
					criteria.lte(DateUtil.parseDateSecondFormat(model.getPublishEndDateStr()));
				}else{
					criteria.and("createDate").lte(DateUtil.parseDateSecondFormat(model.getPublishEndDateStr()));
				}
			}
		}
		return chatMessageDao.findPage(ChatMessage.class, Query.query(criteria), dCriteria);
	}
}
