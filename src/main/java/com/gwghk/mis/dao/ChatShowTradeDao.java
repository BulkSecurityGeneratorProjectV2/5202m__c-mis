package com.gwghk.mis.dao;


import com.gwghk.mis.model.ChatShowTradeComments;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.gwghk.mis.common.dao.MongoDBBaseDao;
import com.gwghk.mis.common.model.DetachedCriteria;
import com.gwghk.mis.common.model.Page;
import com.gwghk.mis.model.BoUser;
import com.gwghk.mis.model.ChatShowTrade;
import com.mongodb.WriteResult;

/**
 * 用户DAO类
 * @author henry.cao
 * @date 2016/6/22
 */
@Repository
public class ChatShowTradeDao extends MongoDBBaseDao{

    public boolean addTrade(ChatShowTrade chatShowTrade){
		this.add(chatShowTrade);
		return true;
	}
    /**
	 * 查询列表
	 * @return
	 */
	public Page<ChatShowTrade> getShowTradePage(Query query,DetachedCriteria<ChatShowTrade> dCriteria){
		return super.findPage(ChatShowTrade.class, query, dCriteria);
	}
	/**
	 * 删除
	 * @param tradeIds
	 * @return
	 */
	public boolean deleteTrade(Object[] tradeIds){
		
		WriteResult wr = this.mongoTemplate.updateMulti(Query.query(Criteria.where("_id").in(tradeIds))
				   , Update.update("valid", 0), ChatShowTrade.class);
		return wr != null && wr.getN() > 0;
	}
	/**
	 * 查看
	 * @param tradeId
	 * @return
	 */
	public ChatShowTrade getTradeById(String tradeId){
		return this.findById(ChatShowTrade.class, tradeId);
	}
	/**
	 * 更新
	 * @param chatShowTrade
	 */
	public void updateTrade(ChatShowTrade chatShowTrade){
		Query query = new Query();
	    query.addCriteria(Criteria.where("id").is(chatShowTrade.getId()));
	    this.update(query, chatShowTrade);
	}
	/**
	 * 同步用户数据
	 * @param boUser
	 * @return
	 */
	public boolean updateTradeByBoUser(BoUser boUser){
	    WriteResult wr=this.mongoTemplate.updateMulti(new Query(Criteria.where("boUser.userNo").is(boUser.getUserNo()).and("systemCategory").is(boUser.getSystemCategory())), new Update()
	    		.set("boUser.avatar", boUser.getAvatar())
	    		.set("boUser.userNo", boUser.getUserNo())
	    		.set("boUser.userName", boUser.getUserName())
	    		.set("boUser.telephone", boUser.getTelephone())
	    		.set("boUser.wechatCode", boUser.getWechatCode())
	    		.set("boUser.wechatCodeImg", boUser.getWechatCodeImg())
	    		.set("boUser.winRate", boUser.getWinRate()),ChatShowTrade.class);
	    
		return wr!=null&&wr.getN()>0;
	}
	
	public ChatShowTrade getById(String id) {
		return this.mongoTemplate.findById(id, ChatShowTrade.class);
	}
	
	/**
	 * 批量更新状态
	 * @param ids
	 * @param status
	 * @return
	 */
	public boolean modifyTradeStatusByIds(Object[] ids, int status){
		WriteResult wr = this.mongoTemplate.updateMulti(Query.query(Criteria.where("_id").in(ids))
				   , Update.update("status", status), ChatShowTrade.class);
		return wr != null && wr.getN() > 0;
	}

	/**
	 * 删除评论
	 * @param sid
	 * @param cid
	 * @return
	 */
	public boolean delComment(String sid, String cid){
		ChatShowTradeComments comments = new ChatShowTradeComments();
		comments.setId(cid);
		WriteResult wr = this.mongoTemplate.updateMulti(Query.query(Criteria.where("_id").is(sid))
				, new Update().pull("comments", comments), ChatShowTrade.class);
		return wr != null && wr.getN() > 0;
	}

	/**
	 * 批量更新晒单盖楼状态
	 * @param ids
	 * @param isAccord
	 * @return
	 */
	public boolean modifyTradeIsAccordByIds(Object[] ids, int isAccord){
		WriteResult wr = this.mongoTemplate.updateMulti(Query.query(Criteria.where("_id").in(ids))
				, Update.update("isAccord", isAccord), ChatShowTrade.class);
		return wr != null && wr.getN() > 0;
	}

  /**
   * 更新晒单评论状态
   * @param tradeId
   * @param commentId
   * @return
   */
	public boolean modifyTradeCommentStatus(String tradeId, String commentId){
		Criteria criteria = new Criteria();
		criteria.and("_id").is(tradeId);
		criteria.and("comments").elemMatch(new Criteria().and("id").is(commentId));
		Update update = new Update();
		update.set("comments.$.status", 1);
		WriteResult wr = this.mongoTemplate.updateFirst(Query.query(criteria), update, ChatShowTrade.class);
		return wr != null && wr.getN() > 0;
  }

}
