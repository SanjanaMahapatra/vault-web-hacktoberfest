package vaultWeb.controllers;

import jakarta.validation.Valid;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import vaultWeb.dtos.ChatMessageDto;
import vaultWeb.models.ChatMessage;
import vaultWeb.services.ChatService;

/**
 * Controller responsible for handling WebSocket-based chat functionality.
 *
 * <p>Supports both group chat and private messages. Messages are first persisted via ChatService
 * and then dispatched to the corresponding topics or users.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

  private final SimpMessagingTemplate messagingTemplate;
  private final ChatService chatService;

  /**
   * Handles incoming group chat messages from clients and broadcasts them to all subscribers of the
   * specified group topic.
   *
   * @param messageDto DTO containing message content, sender information, and target group
   */
  @MessageMapping("/chat.send")
  public void sendMessage(@Payload ChatMessageDto messageDto) {
    ChatMessage savedMessage = chatService.saveMessage(messageDto);

    messagingTemplate.convertAndSend(
        "/topic/group/" + savedMessage.getGroup().getId(), savedMessage);
  }

  /**
   * Handles incoming private chat messages from clients and sends them to both users of the private
   * chat. The message content is decrypted before delivery.
   *
   * @param messageDto DTO containing message content, sender information, and private chat ID
   */
  @MessageMapping("/chat.private.send")
  public void sendPrivateMessage(@Valid @Payload ChatMessageDto messageDto) {
    ChatMessage savedMessage = chatService.saveMessage(messageDto);

    String decryptedContent =
        chatService.decrypt(savedMessage.getCipherText(), savedMessage.getIv());

    ChatMessageDto responseDto = new ChatMessageDto();
    responseDto.setContent(decryptedContent);
    responseDto.setTimestamp(savedMessage.getTimestamp().toString());
    responseDto.setSenderUsername(savedMessage.getSender().getUsername());
    responseDto.setPrivateChatId(savedMessage.getPrivateChat().getId());

    String user1 = savedMessage.getPrivateChat().getUser1().getUsername();
    String user2 = savedMessage.getPrivateChat().getUser2().getUsername();

    messagingTemplate.convertAndSendToUser(user1, "/queue/private", responseDto);
    Set<String> recipients = Set.of(user1, user2);

    recipients.forEach(
        user -> messagingTemplate.convertAndSendToUser(user, "/queue/private", responseDto));

    log.debug(
        "Private message sent from {} to privateChat {}",
        responseDto.getSenderUsername(),
        responseDto.getPrivateChatId());
  }
}
