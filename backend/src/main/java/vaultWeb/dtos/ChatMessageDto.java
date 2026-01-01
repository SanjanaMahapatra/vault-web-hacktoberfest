package vaultWeb.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {
  @NotBlank private String content;
  private String timestamp;
  @NotNull private Long groupId;
  private Long privateChatId;
  private Long senderId;
  private String senderUsername;
}
