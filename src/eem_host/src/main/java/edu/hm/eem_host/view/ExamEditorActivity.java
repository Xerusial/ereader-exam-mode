package edu.hm.eem_host.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import edu.hm.eem_host.R;
import edu.hm.eem_library.model.HASHTOOLBOX;
import edu.hm.eem_library.model.TeacherExam;
import edu.hm.eem_library.model.TeacherExamDocumentItemViewModel;
import edu.hm.eem_library.model.ThumbnailedExamDocument;
import edu.hm.eem_library.view.AbstractExamEditorActivity;
import edu.hm.eem_library.view.TransformedShowCaseView;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

public class ExamEditorActivity extends AbstractExamEditorActivity {

    private static final String SHOWCASE_ID = "ExamEditorActivityHost";

    private EditText pwField;
    private CheckBox allDocAllowedField;
    private boolean allowAnnotations;

    /**
     * Init views, set listeners on view password and all docs allowed checkboxes
     *
     * @param savedInstanceState Android basics
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = ViewModelProviders.of(this).get(TeacherExamDocumentItemViewModel.class);
        setContentView(R.layout.activity_exam_editor);
        pwField = findViewById(R.id.pass);
        allDocAllowedField = findViewById(R.id.allDocsAllowed);
        fileCounter = findViewById(R.id.used_files);
        svButton = findViewById(R.id.bt_save);
        delButton = findViewById(R.id.bt_del_doc);
        addButton = findViewById(R.id.bt_add_doc);
        toolbar = findViewById(R.id.toolbar);
        addButton.setOnClickListener(v -> showSourceDialog());
        progress = findViewById(R.id.progress);
        CheckBox cbPass = findViewById(R.id.showPass);
        cbPass.setOnClickListener(v -> {
            if (cbPass.isChecked()) pwField.setTransformationMethod(null);
            else pwField.setTransformationMethod(new PasswordTransformationMethod());
        });
        docList = findViewById(R.id.doc_list);
        allDocAllowedField.setOnClickListener(v -> {
            if (allDocAllowedField.isChecked()) {
                if (!model.getLivedata().isEmpty())
                    showRemoveAllDocsDialog();
                else docUISetEnabled(false);
            } else {
                docUISetEnabled(true);
            }
        });
    }

    /**
     * Enable adding of documents only when not all documents are allowed
     *
     * @param enable enable document list editor UI
     */
    private void docUISetEnabled(boolean enable) {
        enableButton(addButton, enable);
        docList.setAlpha(enable ? 1.0f : 0.5f);
    }

    /**
     * If user checks all documents allowed checkbox, make sure he did not do this by accident
     */
    private void showRemoveAllDocsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_delete_all_documents))
                .setPositiveButton(getString(android.R.string.ok), (dialog, which) -> {
                    model.getLivedata().clean(false);
                    docUISetEnabled(false);
                })
                .setNegativeButton(getString(android.R.string.no), (dialog, which) -> dialog.cancel())
                .setOnCancelListener(dialog -> allDocAllowedField.setChecked(false))
                .show();
    }

    /**
     * Click listener call of the save button
     *
     * @param v the view of the save button
     */
    @Override
    public void onClick(View v) {
        String pw = pwField.getText().toString();
        if (pw.isEmpty()) {
            Toast.makeText(getApplicationContext(), getString(R.string.toast_enter_password), Toast.LENGTH_SHORT).show();
            return;
        }
        if (model.getLivedata().isEmpty() && !allDocAllowedField.isChecked()) {
            Toast.makeText(getApplicationContext(), getString(R.string.toast_select_documents_teacher), Toast.LENGTH_SHORT).show();
            return;
        }
        ((TeacherExam) model.getCurrent()).setPassword(pw);
        super.onClick(v);
    }

    /**
     * Dialog for adding documents. They can either be specified by page number or by file with or
     * without annotations
     */
    private void showSourceDialog() {
        @SuppressLint("InflateParams") View v = getLayoutInflater().inflate(R.layout.dialog_build_examdocument, null);
        EditText numPages = v.findViewById(R.id.number_pages);
        RadioGroup rg = v.findViewById(R.id.rbs);
        CheckBox allowCb = v.findViewById(R.id.allow_annotations);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(v)
                .setTitle(getString(R.string.dialog_build_document))
                .setPositiveButton(getString(android.R.string.ok), (dialog, which) -> {
                    switch (rg.getCheckedRadioButtonId()) {
                        case R.id.rb_file:
                            allowAnnotations = allowCb.isChecked();
                            checkFileManagerPermissions();
                            break;
                        case R.id.rb_page:
                            int pages;
                            try {
                                pages = Integer.parseInt(numPages.getText().toString());
                            } catch (NumberFormatException e) {
                                Toast.makeText(getApplicationContext(), R.string.toast_specify_pages, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            ThumbnailedExamDocument doc = ThumbnailedExamDocument.getInstance(getApplicationContext(), pages);
                            model.getLivedata().add(doc, false);
                            break;
                    }
                })
                .setNegativeButton(getString(android.R.string.cancel), (dialog, which) -> dialog.cancel())
                .show();
    }

    /**
     * If a document has been returned, load it using a async task
     *
     * @param uri returned uri from the file manager
     */
    @Override
    protected void handleDocument(@Nullable Uri uri) {
        if (uri != null) {
            HostSingleDocumentLoader loader = new HostSingleDocumentLoader(this);
            loader.execute(uri);
        }
    }

    /**
     * A tutorial using a showcase library
     */
    @Override
    protected void tutorial() {
        ShowcaseConfig config = new ShowcaseConfig();

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, SHOWCASE_ID);

        sequence.setConfig(config);

        sequence.addSequenceItem(TransformedShowCaseView.getInstance(this, pwField, R.string.tutorial_pass_field,  -400, 0, 400));

        sequence.addSequenceItem(allDocAllowedField,
                getString(edu.hm.eem_library.R.string.tutorial_all_checkbox), getString(android.R.string.ok));

        sequence.setOnItemDismissedListener((itemView, position) -> {
            if (position == 1)
                super.tutorial();
        });

        sequence.start();
    }

    /**
     * Host variant of the {@link edu.hm.eem_library.view.AbstractExamEditorActivity.SingleDocumentLoader}
     * Has the ability to switch between the desired hashes
     */
    static class HostSingleDocumentLoader extends SingleDocumentLoader {

        HostSingleDocumentLoader(ExamEditorActivity context) {
            super(context);
        }

        @Override
        protected ThumbnailedExamDocument doInBackground(Uri... uris) {
            return ThumbnailedExamDocument.getInstance(context.get(),
                    uris[0], ((ExamEditorActivity) context.get()).allowAnnotations ? HASHTOOLBOX.WhichHash.NON_ANNOTATED : HASHTOOLBOX.WhichHash.NORMAL);
        }

    }
}
