package modeller.lda.data.frame;






import modeller.lda.data.Utils.CollectionUtils;
import modeller.lda.data.Utils.TupleTwo;

import java.io.Serializable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by xschen on 1/5/2017.
 */
public class BasicDataFrame implements DataFrame, Serializable {

    private static final long serialVersionUID = 4096198912048757493L;
    private final List<DataRow> rows = new ArrayList<>();
    private final List<InputDataColumn> inputDataColumns = new ArrayList<>();
    private final List<OutputDataColumn> outputDataColumns = new ArrayList<>();
    private boolean locked = false;
    private final Map<String, List<String>> levels = new HashMap<>();

    @Override public int rowCount() {
        return rows.size();
    }


    @Override public DataRow row(int i) {
        return rows.get(i);
    }


    @Override public List<InputDataColumn> getInputColumns() {
        return inputDataColumns;
    }


    @Override public List<OutputDataColumn> getOutputColumns() {
        return outputDataColumns;
    }


    @Override public List<DataColumn> getAllColumns() {
        List<DataColumn> result = new ArrayList<>();
        result.addAll(inputDataColumns);
        result.addAll(outputDataColumns);
        return result;
    }


    @Override public List<String> rowArrayDescriptors() {
        List<String> numericInputColumns = inputDataColumns.stream().filter(c -> !c.isCategorical()).map(InputDataColumn::getColumnName).collect(Collectors.toList());
        List<String> categoricalInputColumns = inputDataColumns.stream().filter(InputDataColumn::isCategorical).map(InputDataColumn::getColumnName).collect(Collectors.toList());

        List<String> result = new ArrayList<>(numericInputColumns);
        for(String c : categoricalInputColumns){
            List<String> levelsInFactor = levels.get(c);
            int count = levelsInFactor.size();
            if(count == 2) count = 1;
            for(int j=0; j < count;++j){
                result.add(c + ":" + levelsInFactor.get(j));
            }
        }
        return result;
    }


    @Override public void unlock(){
        locked = false;
    }

    @Override
    public boolean isLocked() {
        return locked;
    }


    @Override public void lock() {

        Map<String, Set<String>> inputLevels = new HashMap<>();
        Map<String, Set<String>> outputLevels = new HashMap<>();

        for(DataRow row : rows){
            List<String> keys = row.getColumnNames();
            for(String key: keys) {
                Set<String> set;

                if(!inputLevels.containsKey(key)){
                    set = new HashSet<>();
                    inputLevels.put(key, set);
                }
            }

            keys = row.getCategoricalColumnNames();
            for(String key: keys) {
                Set<String> set;

                if(inputLevels.containsKey(key)){
                    set = inputLevels.get(key);
                } else {
                    set = new HashSet<>();
                    inputLevels.put(key, set);
                }

                set.add(row.getCategoricalCell(key));
            }

            keys = row.getTargetColumnNames();
            for(String key: keys) {
                Set<String> set;

                if(!outputLevels.containsKey(key)){
                    set = new HashSet<>();
                    outputLevels.put(key, set);
                }
            }

            keys = row.getCategoricalTargetColumnNames();
            for(String key: keys) {
                Set<String> set;

                if(outputLevels.containsKey(key)){
                    set = outputLevels.get(key);
                } else {
                    set = new HashSet<>();
                    outputLevels.put(key, set);
                }

                set.add(row.getCategoricalTargetCell(key));
            }
        }

        inputDataColumns.clear();
        for(Map.Entry<String, Set<String>> entry : inputLevels.entrySet()){
            Set<String> set = entry.getValue();
            InputDataColumn inputDataColumn = new InputDataColumn();
            inputDataColumn.setColumnName(entry.getKey());

            List<String> levels = set.stream().collect(Collectors.toList());
            levels.sort(String::compareTo);
            inputDataColumn.setLevels(levels);
            inputDataColumns.add(inputDataColumn);
        }

        outputDataColumns.clear();
        for(Map.Entry<String, Set<String>> entry : outputLevels.entrySet()){
            Set<String> set = entry.getValue();
            OutputDataColumn outputDataColumn = new OutputDataColumn();
            outputDataColumn.setColumnName(entry.getKey());

            List<String> levels = set.stream().collect(Collectors.toList());
            levels.sort(String::compareTo);
            outputDataColumn.setLevels(levels);
            outputDataColumns.add(outputDataColumn);
        }

        inputDataColumns.sort((a, b) -> a.getColumnName().compareTo(b.getColumnName()));
        outputDataColumns.sort((a, b) -> a.getColumnName().compareTo(b.getColumnName()));

        List<String> numericInputColumns = inputDataColumns.stream().filter(c -> !c.isCategorical()).map(InputDataColumn::getColumnName).collect(Collectors.toList());
        List<String> categoricalInputColumns = inputDataColumns.stream().filter(InputDataColumn::isCategorical).map(InputDataColumn::getColumnName).collect(Collectors.toList());
        List<String> numericOutputColumns = outputDataColumns.stream().filter(c -> !c.isCategorical()).map(OutputDataColumn::getColumnName).collect(Collectors.toList());
        List<String> categoricalOutputColumns = outputDataColumns.stream().filter(OutputDataColumn::isCategorical).map(OutputDataColumn::getColumnName).collect(Collectors.toList());

        numericInputColumns.sort(String::compareTo);
        categoricalInputColumns.sort(String::compareTo);
        numericOutputColumns.sort(String::compareTo);
        categoricalOutputColumns.sort(String::compareTo);

        levels.clear();

        for(Map.Entry<String, Set<String>> entry : inputLevels.entrySet()){
            List<String> levelsInFactor = entry.getValue().stream().collect(Collectors.toList());
            levelsInFactor.sort(String::compareTo);
            levels.put(entry.getKey(), levelsInFactor);
        }

        for(Map.Entry<String, Set<String>> entry : outputLevels.entrySet()){
            List<String> levelsInFactor = entry.getValue().stream().collect(Collectors.toList());
            levelsInFactor.sort(String::compareTo);
            levels.put(entry.getKey(), levelsInFactor);
        }

        for(int i=0; i < rowCount(); ++i) {
            DataRow row = row(i);
            row.setColumnNames(numericInputColumns);
            row.setCategoricalColumnNames(categoricalInputColumns);
            row.setTargetColumnNames(numericOutputColumns);
            row.setCategoricalTargetColumnNames(categoricalOutputColumns);
            row.setLevels(levels);
        }

        locked = true;
    }


    @Override public DataRow newRow() {
        return new BasicDataRow();
    }


    @Override public void addRow(DataRow row) {
        if(locked) {
            throw new RuntimeException("Data frame is currently locked, please unlock first");
        }
        rows.add(row);
    }


    @Override public String head(int limit) {
        StringBuilder sb = new StringBuilder();
        int max = Math.min(limit, rowCount());
        for(int i=0; i < max; ++i) {
            if(i != 0){
                sb.append("\n");
            }
            sb.append(row(i));
        }
        return sb.toString();
    }


    @Override public DataFrame shuffle() {
        Random random = new Random(System.currentTimeMillis());
        for(int i=1; i < rows.size(); ++i) {
            int j = random.nextInt(i+1);
            CollectionUtils.exchange(rows, i, j);
        }
        return this;
    }


    @Override public TupleTwo<DataFrame, DataFrame> split(double ratio) {
        assert this.locked;

        BasicDataFrame frame1 = new BasicDataFrame();
        BasicDataFrame frame2 = new BasicDataFrame();

        frame1.inputDataColumns.addAll(inputDataColumns.stream().map(InputDataColumn::makeCopy).collect(Collectors.toList()));
        frame2.inputDataColumns.addAll(inputDataColumns.stream().map(InputDataColumn::makeCopy).collect(Collectors.toList()));

        frame1.outputDataColumns.addAll(outputDataColumns.stream().map(OutputDataColumn::makeCopy).collect(Collectors.toList()));
        frame2.outputDataColumns.addAll(outputDataColumns.stream().map(OutputDataColumn::makeCopy).collect(Collectors.toList()));

        int split = (int)(rows.size() * ratio);
        for(int i=0; i < split; ++i) {
            frame1.addRow(rows.get(i).makeCopy());
        }
        for(int i=split; i < rows.size(); ++i){
            frame2.addRow(rows.get(i).makeCopy());
        }

        return new TupleTwo<>(frame1, frame2);

    }


    @Override public Stream<DataRow> stream() {
        return rows.stream();
    }


    @Override public DataFrame makeCopy() {
        BasicDataFrame clone = new BasicDataFrame();
        clone.copy(this);
        return clone;
    }

    private void copy(DataFrame that){
        rows.clear();
        inputDataColumns.clear();
        outputDataColumns.clear();
        levels.clear();

        unlock();

        for(DataRow row : that.rows()){
            DataRow newRow = newRow();
            newRow.copy(row);
            addRow(row);
        }

        lock();
    }


    @Override public DataFrame filter(Predicate<DataRow> predicate) {
        DataFrame clone = new BasicDataFrame();
        for(DataRow row : rows){
            if(predicate.test(row)){
                DataRow newRow = clone.newRow();
                newRow.copy(row);
            }
        }
        clone.lock();
        return clone;
    }


    @Override public Iterable<? extends DataRow> rows() {
        return rows;
    }


    @Override public Iterator<DataRow> iterator() {
        return rows.iterator();
    }

    @Override public Map<String, List<String>> getLevels(){
        return levels;
    }
}
