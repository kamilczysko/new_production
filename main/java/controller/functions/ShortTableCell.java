package controller.functions;

import java.util.regex.Pattern;

import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import model.Batch;

public class ShortTableCell extends TableCell<Batch, Short>{

	 private final TextField textField = new TextField();
     private final Pattern intPattern = Pattern.compile("-?\\d+");

     public ShortTableCell() {
         textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
             if (! isNowFocused) {
                 processEdit();
             }
         });
         textField.setOnAction(event -> processEdit());
     }

     private void processEdit() {
         String text = textField.getText();
         if (intPattern.matcher(text).matches()) {
             commitEdit(Short.parseShort(text));
         } else {
             cancelEdit();
         }
     }

     public void updateItem(Short value, boolean empty) {
         super.updateItem(value, empty);
         if (empty) {
             setText(null);
             setGraphic(null);
         } else if (isEditing()) {
             setText(null);
             textField.setText(value.toString());
             setGraphic(textField);
         } else {
             setText(value.toString());
             setGraphic(null);
         }
     }

     @Override
     public void startEdit() {
         super.startEdit();
         Number value = getItem();
         if (value != null) {
             textField.setText(value.toString());
             setGraphic(textField);
             setText(null);
         }
     }

     @Override
     public void cancelEdit() {
         super.cancelEdit();
         setText(getItem().toString());
         setGraphic(null);
     }

     // This seems necessary to persist the edit on loss of focus; not sure why:
     public void commitEdit(Short value) {
         super.commitEdit(value);
         ((Batch)this.getTableRow().getItem()).setIlosc(value.shortValue());
     }
 }


