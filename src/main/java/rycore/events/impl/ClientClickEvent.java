package rycore.events.impl;

import net.minecraft.text.ClickEvent;
import net.minecraft.text.ClickEvent.Action;

/**
 * Click event rieng cua RyCore de chat co the kich hoat lenh client.
 *
 * <p>Tu 1.21.5 {@link ClickEvent} tro thanh interface (moi Action la mot record
 * rieng) nen khong con extends duoc; lop nay implements no va tu giu chuoi lenh.
 */
public record ClientClickEvent(String value) implements ClickEvent {
   /** Giu nguyen chu ky cu de cac cho goi khong phai sua theo. */
   public ClientClickEvent(Action action, String value) {
      this(value);
   }

   @Override
   public Action getAction() {
      return Action.RUN_COMMAND;
   }

   /** Thay cho {@code ClickEvent.getValue()} da bi bo khoi interface. */
   public String getValue() {
      return this.value;
   }
}
