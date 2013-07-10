package org.apache.solr.handler.dataimport;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConditionalCopyTransformer extends Transformer {
  private static Logger log = LoggerFactory
      .getLogger(ConditionalCopyTransformer.class);

  @Override
  public Object transformRow(Map<String,Object> row, Context context) {
    List<Map<String,String>> fields = context.getAllEntityFields();

    for (Map<String,String> field : fields) {
      String copyTo = field.get("copyTo");
      if (copyTo != null) {
        if (field.containsKey("fieldsToCheck") &&
            field.containsKey("valuesToMatch") &&
            field.containsKey(DataImporter.COLUMN)) {

          Boolean keepOrig = field.get("keepOrig") == null ? true : Boolean
              .parseBoolean(field.get("keepOrig"));
          String[] copyToSuffixes = copyTo.split(",");
          String columnName = field.get(DataImporter.COLUMN);
          String[] fieldsToCheck = field.get("fieldsToCheck").split(",");
          String[] valuesToMatch = field.get("valuesToMatch").split(",");

          for (int i = 0; i < copyToSuffixes.length; i++) {
            if (row.containsKey(fieldsToCheck[i])) {
              String fieldValue = row.get(fieldsToCheck[i]).toString();
              if (fieldValue.equals(valuesToMatch[i])) {
                row.put(columnName + "_" + copyToSuffixes[i], row.get(columnName));
                if (!keepOrig) {
                  row.put(columnName, "");
                }
              }
            } else {
              log.error(
                  "Field specified in fieldsToCheck for {} does not exist: {}.",
                  columnName, fieldsToCheck[i]);
            }
          }
        } else {
          log.error(
              "Missing parameter on field {} in DB-config.xml."
                  + "Fields using the ConditionalCopyProcessor must specify parameters:\n"
          + "- copyTo=<Comma-separated suffixes of new fields to copy to>,\n"
          + "- fieldsToCheck=<Comma-seprarated existing fields to check conditions on>,\n"
                  + "- matchValues=<Comma-separated values to check for in fieldsToCheck>.",
              field.get(DataImporter.COLUMN));
        }
      }
    }
    return row;
  }
}
