package com.iosite.io_safesite.Util;

import org.json.JSONArray;
import org.json.JSONObject;

public class NotificationUtil {

    private String receipts_data;
    private String receiver_uid;
    private String sender_uid;
    private String conversationId;
    private String receiverType;
    private String id;
    private String sentAt;
    private String category;
    private String type;
    private String updatedAt;

    private String msg_text;
    private String receiver_entityType;
    private String sender_entityType;

    private String receiver_entity_lastActiveAt;
    private String receiver_entity_uid;
    private String receiver_entity_role;
    private String receiver_entity_name;
    private String receiver_entity_status;

    private String sender_entity_lastActiveAt;
    private String sender_entity_uid;
    private String sender_entity_role;
    private String sender_entity_name;
    private String sender_entity_status;

    /*when receiver is group*/
    private String receiver_entity_group_owner;
    private String receiver_entity_group_createdAt;
    private String receiver_entity_group_joinedAt;
    private String receiver_entity_group_scope;
    private String receiver_entity_group_name;
    private String receiver_entity_group_guid;
    private String receiver_entity_group_type;
    private String receiver_entity_group_hasJoined;

    private String audio_local_path;
    private String url;
    private String audio_muid;

    private String audio_extension;
    private String ausio_size;
    private String audio_name;
    private String audio_mimeType;
    private String audio_url;

    private String action_gM_action;

    private String action_gM_entities_by_entity_name;
    private String action_gM_entities_for_entity_name;
    private String action_gM_entities_for_entity_guid;
    private String action_gM_entities_on_entity_name;
    private String action_gM_entities_on_entity_uid;

    public NotificationUtil(String message){
        JSONObject msgNotificationJson = null;

        try {
            msgNotificationJson = new JSONObject(message);

            JSONObject data = msgNotificationJson.getJSONObject("data");
            this.receipts_data = msgNotificationJson.getJSONObject("receipts").getString("data");
            this.receiver_uid = msgNotificationJson.getString("receiver");
            this.sender_uid =  msgNotificationJson.getString("sender");
            this.conversationId = msgNotificationJson.getString("conversationId");
            this.receiverType =  msgNotificationJson.getString("receiverType");
            this.id = msgNotificationJson.getString("id");
            this.sentAt =  msgNotificationJson.getString("sentAt");
            this.category = msgNotificationJson.getString("category");
            this.type =  msgNotificationJson.getString("type");
            this.updatedAt = msgNotificationJson.getString("updatedAt");

            if(this.category.equals("message")){
                JSONObject entities = data.getJSONObject("entities");
                JSONObject receiver =   entities.getJSONObject("receiver");
                JSONObject sender =   entities.getJSONObject("sender");
                this.receiver_entityType = receiver.getString("entityType");
                JSONObject receiver_entity = receiver.getJSONObject("entity");
                this.sender_entityType = sender.getString("entityType");
                JSONObject sender_entity = sender.getJSONObject("entity");

                this.sender_entity_lastActiveAt = sender_entity.getString("lastActiveAt");
                this.sender_entity_uid = sender_entity.getString("uid");
                this.sender_entity_role = sender_entity.getString("role");
                this.sender_entity_name = sender_entity.getString("name");
                this.sender_entity_status = sender_entity.getString("status");

                if(this.type.equals("audio")){
                    // TODO: handle category, type and url in case of audio msg. These field are available already
                    JSONArray attachmentsArray = data.getJSONArray("attachments");
                    this.url = data.getString("url");
                    for(int k = 0; k < attachmentsArray.length(); k++){
                        JSONObject attachments = attachmentsArray.getJSONObject(k);
                        this.audio_extension = attachments.getString("extension");
                        this.ausio_size = attachments.getString("size");
                        this.audio_name = attachments.getString("name");
                        this.audio_mimeType = attachments.getString("mimeType");
                        this.audio_url = attachments.getString("url");
                    }
                } else if(this.type.equals("text")){
                    this.msg_text = data.getString("text");
                }

                if(this.receiverType.equals("user")){
                    this.receiver_entity_lastActiveAt = receiver_entity.getString("lastActiveAt");
                    this.receiver_entity_uid = receiver_entity.getString("uid");
                    this.receiver_entity_role = receiver_entity.getString("role");
                    this.receiver_entity_name = receiver_entity.getString("name");
                    this.receiver_entity_status = receiver_entity.getString("status");
                } else if(this.receiverType.equals("group")){
                    this.receiver_entity_group_owner = receiver_entity.getString("owner");
                    this.receiver_entity_group_createdAt = receiver_entity.getString("createdAt");
                    this.receiver_entity_group_joinedAt = receiver_entity.getString("joinedAt");
                    this.receiver_entity_group_scope = receiver_entity.getString("scope");
                    this.receiver_entity_group_name = receiver_entity.getString("name");
                    this.receiver_entity_group_guid = receiver_entity.getString("guid");
                    this.receiver_entity_group_type = receiver_entity.getString("type");
                    this.receiver_entity_group_hasJoined = receiver_entity.getString("hasJoined");
                }
            } else if(this.category.equals("action")){
                if(this.type.equals("groupMember")){
                    this.action_gM_action = data.getString("action");
                    this.action_gM_entities_on_entity_name = data.getJSONObject("entities").getJSONObject("on").getJSONObject("entity").getString("name");
                    this.action_gM_entities_on_entity_uid = data.getJSONObject("entities").getJSONObject("on").getJSONObject("entity").getString("uid");
                    this.action_gM_entities_for_entity_name = data.getJSONObject("entities").getJSONObject("for").getJSONObject("entity").getString("name");
                    this.action_gM_entities_for_entity_guid = data.getJSONObject("entities").getJSONObject("for").getJSONObject("entity").getString("guid");
                    this.action_gM_entities_by_entity_name = data.getJSONObject("entities").getJSONObject("by").getJSONObject("entity").getString("name");
                }
            }

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public String getId(){return this.id;}
    public String getAudioMsgLocalPath(){return this.audio_local_path;}
    public String getAudioMsgUrl(){return this.audio_url;}
    public String getSenderEntityUid(){return this.sender_entity_uid;}
    public String getSenderEntityName(){return this.sender_entity_name;}

    /*group or user msg*/
    public String getReceiverType(){return this.receiverType;}

    /*get group details*/
    public String getGroupName(){return this.receiver_entity_group_name; }
    public String getGroupGuid(){return this.receiver_entity_group_guid; }

    public String getMsgText(){return this.msg_text;}

    public String getMsgType(){return this.type;}
    public String getMsgCategory(){return this.category;}

    public String getActionGMAction(){return this.action_gM_action;}
    public String getActionGMEntitiesOnEntityName(){return this.action_gM_entities_on_entity_name;}
    public String getActionGMEntitiesForEntityName(){return this.action_gM_entities_for_entity_name;}
    public String getActionGMEntitiesForEntityGuid(){return this.action_gM_entities_for_entity_guid;}
    public String getActionGMEntitiesByEntityName(){return this.action_gM_entities_by_entity_name;}
    public String getActionGMEntitiesOnEntityUid(){return this.action_gM_entities_on_entity_uid;}


}
