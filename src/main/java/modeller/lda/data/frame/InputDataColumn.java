package modeller.lda.data.frame;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xschen on 29/4/2017.
 */
public class InputDataColumn implements Serializable, DataColumn {

    private static final long serialVersionUID = -3355436850471047863L;
    private int sourceColumnIndex;
    private String columnName;
    private final List<String> levels = new ArrayList<>();

    public InputDataColumn(){

    }

    public InputDataColumn(String columnName) {
        this.columnName = columnName;
    }

    public InputDataColumn makeCopy() {
        InputDataColumn clone = new InputDataColumn();

        clone.copy(this);
        return clone;
    }

    public void copy(InputDataColumn that) {
        this.sourceColumnIndex = that.sourceColumnIndex;
        this.columnName = that.columnName;
        this.levels.clear();
        this.levels.addAll(that.levels);
    }

    public boolean isCategorical(){
        return !levels.isEmpty();
    }

    public void setSourceColumnIndex(int key) {
        this.sourceColumnIndex = key;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setLevels(List<String> levels) {
        this.levels.clear();
        this.levels.addAll(levels);
    }

    public List<String> getLevels(){
        return levels;
    }

    @Override
    public String toString(){
        return columnName;
    }


    public String summary() {
        return columnName + ":discrete=" + levels.size();
    }

    @Override
    public boolean isOutputColumn(){
        return false;
    }
}
