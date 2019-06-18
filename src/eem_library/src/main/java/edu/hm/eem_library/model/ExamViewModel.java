package edu.hm.eem_library.model;

import android.app.Application;

import androidx.annotation.Nullable;

import java.util.Set;

public abstract class ExamViewModel<T extends StudentExam> extends FilebackedItemViewModel<ExamViewModel.ExamDocumentLiveData> {
    private T current;
    private String currentName;
    protected ExamFactory factory;

    public ExamViewModel(Application application) {
        super(application);
    }

    public class ExamDocumentLiveData extends SelectableSortableMapLiveData<ExamDocument, ThumbnailedExamDocument>{
        ExamDocumentLiveData(@Nullable Set<ThumbnailedExamDocument> set, boolean notificationNeeded) {
            super(set, notificationNeeded);
        }

        @Override
        public boolean add(ThumbnailedExamDocument container, boolean post) {
            while(!super.add(container, post)){
                container.incrKey();
            }
            return true;
        }

        public void setRejected(int index, ThumbnailedExamDocument.RejectionReason reason){
            getValue().get(index).reason = reason;
            toggleSelected(index);
        }
    }

    public void openExam(String name) {
        current = (T) factory.get(examDir,name);
        currentName = name;
        this.livedata = new ExamDocumentLiveData(current.toLiveDataSet(getApplication()), true);
    }

    public void closeExam(){
        current.documentsFromLivedata(livedata.backingMap.values());
        factory.writeExamToFile(current, examDir, currentName);
    }

    public T getCurrent() {
        return current;
    }
}

