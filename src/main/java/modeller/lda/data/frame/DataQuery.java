package modeller.lda.data.frame;




import modeller.lda.data.Utils.CsvUtils;
import modeller.lda.data.Utils.NumberUtils;
import modeller.lda.data.Utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class DataQuery {

    public interface DataFrameQueryBuilder {
        DataFrameQueryBuilder skipRows(int skippedRowCount);
        DataColumnBuilder selectColumn(int columnIndex);
        DataFrame build();
    }

    public interface DataColumnBuilder {
        DataColumnBuilder transform(Function<String, Object> columnTransformer);
        default DataColumnBuilder asNumeric(){
            return transform(StringUtils::parseDouble);
        }
        default DataColumnBuilder asCategory(){
            return transform(String::trim);
        }
        DataFrameQueryBuilder asInput(String columnName);
        DataFrameQueryBuilder asOutput(String columnName);
    }

    public interface FormatBuilder {
        @Deprecated
        SourceBuilder csv(String splitter, boolean skipFirstLine);
        SourceBuilder csv(String splitter);
        default SourceBuilder csv() {
            return csv("\\s");
        }

        SourceBuilder libsvm();
        DataTableBuilder blank();
    }

    public interface  DataTableBuilder {
        DataTableBuilder newInput(String columnName);
        DataTableBuilder newOutput(String columnName);
        DataFrameQueryBuilder end();
    }

    public interface SourceBuilder {
        DataFrameQueryBuilder from(InputStream inputStream);
    }

    private static class DataFrameColumn {
        private int index;
        private Function<String, Object> transformer;
        private String columnName;

        public DataFrameColumn(String columnName, int index, Function<String, Object> transformer){
            this.columnName = columnName;
            this.index = index;
            this.transformer = transformer;
        }
    }

    private static class DataFrameBuilderX implements SourceBuilder, DataFrameQueryBuilder, DataColumnBuilder, FormatBuilder, DataTableBuilder {

        private final List<DataFrameColumn> inputColumns = new ArrayList<>();
        private final List<DataFrameColumn> outputColumns = new ArrayList<>();
        private InputStream dataInputStream;
        private String csvSplitter = "\\s";
        private DataFileType fileType;

        @Deprecated
        private boolean skipFirstLine = false;
        private int skippedRowCount = 0;

        private static final Logger logger = LoggerFactory.getLogger(DataFrameBuilderX.class);

        private DataFrameColumn selected = null;

        @Override public DataColumnBuilder selectColumn(int columnIndex) {
            selected = new DataFrameColumn("", columnIndex, x -> x);
            return this;
        }

        @Override public DataFrameQueryBuilder skipRows(int skippedRowCount) {
            this.skippedRowCount = skippedRowCount;
            return this;
        }

        @Override public DataFrame build() {
            final BasicDataFrame dataFrame = new BasicDataFrame();

            if(fileType == DataFileType.Csv) {
                if(inputColumns.isEmpty()){
                    throw new RuntimeException("data frame should not have empty input columns");
                }

                int skippedLines = Math.max(this.skipFirstLine ? 1 : 0, this.skippedRowCount);

                CsvUtils.csv(dataInputStream, csvSplitter, skippedLines, (words) -> {
                    DataRow row = dataFrame.newRow();

                    for (int i = 0; i < words.length; ++i) {
                        for (DataFrameColumn c : inputColumns) {
                            if (c.index == i) {
                                Object data = c.transformer.apply(words[i]);
                                if(data instanceof String){
                                    row.setCategoricalCell(c.columnName, (String)data);
                                } else {
                                    row.setCell(c.columnName, NumberUtils.toDouble(data));
                                }
                            }
                        }
                        for (DataFrameColumn c : outputColumns) {
                            if (c.index == i) {
                                Object target = c.transformer.apply(words[i]);
                                if(target instanceof String) {
                                    row.setCategoricalTargetCell(c.columnName, (String)target);
                                } else {
                                    row.setTargetCell(c.columnName, NumberUtils.toDouble(target));
                                }
                            }
                        }
                    }

                    dataFrame.addRow(row);
                    return true;
                }, (e) -> logger.error("Failed to read csv file", e));
            } else if(fileType == DataFileType.HeartScale) {
                List<Map<Integer, String>> rows = CsvUtils.readHeartScale(dataInputStream);
                if(inputColumns.isEmpty() && outputColumns.isEmpty()) {
                    for(Map<Integer, String> row : rows) {
                        DataRow newRow = dataFrame.newRow();
                        for(Map.Entry<Integer, String> entry : row.entrySet()){

                            int columnIndex = entry.getKey();
                            if(columnIndex != 0) {
                                newRow.setCell("" + columnIndex, StringUtils.parseDouble(entry.getValue()));
                            } else {
                                newRow.setTargetCell("label", StringUtils.parseDouble(entry.getValue()));
                            }
                        }
                        dataFrame.addRow(newRow);
                    }
                } else if(inputColumns.isEmpty() || outputColumns.isEmpty()) {
                    throw new RuntimeException("data frame should not have either empty input columns or empty output columns");
                } else {
                    for (Map<Integer, String> row : rows) {
                        DataRow newRow = dataFrame.newRow();
                        for (DataFrameColumn c : inputColumns) {
                            Object data = c.transformer.apply(row.get(c.index));
                            if(data instanceof String) {
                                newRow.setCategoricalCell(c.columnName, (String)data);
                            } else {
                                newRow.setCell(c.columnName, NumberUtils.toDouble(data));
                            }
                        }
                        for (DataFrameColumn c : outputColumns) {
                            Object target = c.transformer.apply(row.get(c.index));
                            if(target instanceof String) {
                                newRow.setCategoricalTargetCell(c.columnName, (String) target);
                            } else {
                                newRow.setTargetCell(c.columnName, NumberUtils.toDouble(target));
                            }
                        }
                        dataFrame.addRow(newRow);
                    }
                }
            } else if(fileType == DataFileType.Memory) {
                dataFrame.getInputColumns().clear();
                dataFrame.getOutputColumns().clear();

                for(DataFrameColumn c : inputColumns) {
                    dataFrame.getInputColumns().add(new InputDataColumn(c.columnName));
                }
                for(DataFrameColumn c : outputColumns) {
                    dataFrame.getOutputColumns().add(new OutputDataColumn(c.columnName));
                }
            }

            if(fileType != DataFileType.Memory) {
                dataFrame.lock();
            }

            return dataFrame;
        }

        @Deprecated
        @Override public SourceBuilder csv(String splitter, boolean skipFirstLine) {
            this.skipFirstLine = skipFirstLine;
            csvSplitter = splitter;
            fileType = DataFileType.Csv;
            return this;
        }

        @Override public SourceBuilder csv(String splitter){
            csvSplitter = splitter;
            fileType = DataFileType.Csv;
            return this;
        }

        @Override public DataFrameQueryBuilder from(InputStream inputStream) {
            dataInputStream = inputStream;
            return this;
        }


        @Override public SourceBuilder libsvm() {
            fileType = DataFileType.HeartScale;
            return this;
        }


        @Override public DataTableBuilder blank() {
            fileType = DataFileType.Memory;
            return this;
        }


        @Override public DataColumnBuilder transform(Function<String, Object> columnTransformer) {
            selected.transformer = columnTransformer;
            return this;
        }


        @Override public DataFrameQueryBuilder asInput(String columnName) {
            selected.columnName = columnName;
            inputColumns.add(selected);
            selected = null;
            return this;
        }


        @Override public DataFrameQueryBuilder asOutput(String columnName) {
            selected.columnName = columnName;
            outputColumns.add(selected);
            selected = null;
            return this;
        }


        @Override public DataTableBuilder newInput(String columnName) {
            inputColumns.add(new DataFrameColumn(columnName, -1, StringUtils::parseDouble));
            return this;
        }


        @Override public DataTableBuilder newOutput(String columnName) {
            outputColumns.add(new DataFrameColumn(columnName, -1, StringUtils::parseDouble));
            return this;
        }


        @Override public DataFrameQueryBuilder end() {
            if(inputColumns.isEmpty()){
                throw new RuntimeException("input columns cannot be empty!");
            }
            if(outputColumns.isEmpty()) {
                throw new RuntimeException("output columns cannot be empty!");
            }
            return this;
        }
    }


    public static SourceBuilder libsvm() {
        return new DataFrameBuilderX().libsvm();
    }

    @Deprecated
    public static SourceBuilder csv(String splitter, boolean skipFirstLine) {
        return new DataFrameBuilderX().csv(splitter, skipFirstLine);
    }

    public static SourceBuilder csv(String splitter) {
        return new DataFrameBuilderX().csv(splitter);
    }

    public static SourceBuilder csv(){
        return new DataFrameBuilderX().csv();
    }

    public static DataTableBuilder blank() {
        return new DataFrameBuilderX().blank();
    }
}
