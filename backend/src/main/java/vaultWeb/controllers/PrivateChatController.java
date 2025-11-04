package vaultWeb.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vaultWeb.dtos.ChatMessageDto;
import vaultWeb.dtos.PrivateChatDto;
import vaultWeb.models.ChatMessage;
import vaultWeb.models.PrivateChat;
import vaultWeb.repositories.ChatMessageRepository;
import vaultWeb.security.EncryptionUtil;
import vaultWeb.services.PrivateChatService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/private-chats")
@Tag(name = "Private Chat Controller", description = "Handles private chats between users, including chat creation and message retrieval")
@RequiredArgsConstructor
public class PrivateChatController {

    private final PrivateChatService privateChatService;
    private final ChatMessageRepository chatMessageRepository;
    private final EncryptionUtil encryptionUtil;

    @GetMapping("/between")
    @Operation(
            summary = "Get or create a private chat between two users",
            description = """
                    This endpoint retrieves an existing private chat between two users, or creates a new one if it does not exist.
                    - 'sender' and 'receiver' are the usernames of the users.
                    - Returns a PrivateChatDto containing the chat ID and the usernames of both participants.
                    """
    )
    public PrivateChatDto getOrCreatePrivateChat(
            @RequestParam String sender,
            @RequestParam String receiver
    ) {
        PrivateChat chat = privateChatService.getOrCreatePrivateChat(sender, receiver);
        return new PrivateChatDto(chat.getId(), chat.getUser1().getUsername(), chat.getUser2().getUsername());
    }

    @GetMapping("/private")
    @Operation(
            summary = "Get all messages of a private chat",
            description = """
                    Retrieves all messages from a specific private chat.
                    - 'privateChatId' is the ID of the private chat.
                    - Messages are ordered chronologically by timestamp.
                    - The message content is decrypted before being sent to the client.
                    - Returns a list of ChatMessageDto containing decrypted content, sender info, timestamp, and chat ID.
                    """
    )
    public List<ChatMessageDto> getPrivateChatMessages(@RequestParam Long privateChatId) {
        List<ChatMessage> messages = chatMessageRepository.findByPrivateChatIdOrderByTimestampAsc(privateChatId);

        return messages.stream()
                .map(message -> {
                    try {
                        String decryptedContent = encryptionUtil.decrypt(message.getCipherText(), message.getIv());
                        return new ChatMessageDto(
                                decryptedContent,
                                message.getTimestamp().toString(),
                                null,
                                privateChatId,
                                message.getSender().getId(),
                                message.getSender().getUsername()
                        );
                    } catch (Exception e) {
                        throw new RuntimeException("Decryption failed", e);
                    }
                })
                .collect(Collectors.toList());
    }
}
