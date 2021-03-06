package edu.hm.eem_library.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Objects;

import edu.hm.eem_library.R;
import edu.hm.eem_library.model.ExamDocument;
import edu.hm.eem_library.model.SelectableSortableItemLiveData;
import edu.hm.eem_library.model.ThumbnailedExamDocument;

/**
 * Subclass of {@link ItemRecyclerViewAdapter}. For more info on the {@link ItemRecyclerViewAdapter} family, check out
 * {@link ItemRecyclerViewAdapter}.
 */
public class ExamDocumentRecyclerViewAdapter extends InteractableItemRecyclerViewAdapter {
    private final int colorBlackOpaque;
    private final int colorPrimaryOpaque;

    /**
     * Constructor
     *
     * @param liveData     liveData holding the data for this adapter
     * @param context      calling activity
     * @param listener     listener for on item clicks
     * @param content      Indicator for the recyclerview fragment holding this adapter
     * @param isSelectable Are tabs selectable by user?
     */
    ExamDocumentRecyclerViewAdapter(SelectableSortableItemLiveData<ExamDocument, ThumbnailedExamDocument> liveData, Context context, ItemListFragment.OnListFragmentPressListener listener, ItemListContent content, boolean isSelectable) {
        super(liveData, listener, content, isSelectable);
        this.colorBlackOpaque = context.getColor(R.color.colorBlackOpaque);
        this.colorPrimaryOpaque = context.getColor(R.color.colorPrimaryOpaque);
    }

    /**
     * Create new viewholder
     *
     * @param parent   Android basics
     * @param viewType Android basics
     * @return new viewholder
     */
    @NonNull
    @Override
    public StringViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.document_item, parent, false);
        return new DocumentViewHolder(v);
    }

    /**
     * The viewholder for this adapter
     */
    class DocumentViewHolder extends SelectableStringViewHolder {
        final ImageView thumbnail;
        final TextView numberPages;
        final ConstraintLayout item;
        final ImageView notes_allowed;


        /**
         * Constructor
         *
         * @param view inflated view of this viewholder
         */
        DocumentViewHolder(View view) {
            super(view);
            thumbnail = view.findViewById(R.id.icon);
            numberPages = view.findViewById(R.id.number_of_pages);
            item = view.findViewById(R.id.item);
            notes_allowed = view.findViewById(R.id.notes_allowed);
        }

        /**
         * Initialize this viewholder from livedata
         *
         * @param position view is at this position in list
         */
        @Override
        void initializeFromLiveData(int position) {
            super.initializeFromLiveData(position);
            ThumbnailedExamDocument ted = (ThumbnailedExamDocument) Objects.requireNonNull(liveData.getValue()).get(position);
            if (isSelectable) {
                if (ted.hasThumbnail) {
                    if (ted.thumbnail != null) {
                        thumbnail.setImageBitmap(ted.thumbnail);
                        numberPages.setVisibility(View.INVISIBLE);
                    } else {
                        numberPages.setText(R.string.thumbnail_not_found);
                        numberPages.setVisibility(View.VISIBLE);
                        numberPages.setTextSize(18);
                    }
                } else {
                    numberPages.setText(String.valueOf(ted.item.getPages()));
                    numberPages.setVisibility(View.VISIBLE);
                    numberPages.setTextSize(36);
                }
                notes_allowed.setVisibility(ted.item.getHash() == null ? View.VISIBLE : View.GONE);
            } else {
                thumbnail.setImageBitmap(ted.thumbnail);
                switch (ted.reason) {
                    case TOO_MANY_PAGES:
                        numberPages.setText(R.string.too_many_pages);
                        break;
                    case HASH_DOES_NOT_MATCH:
                        numberPages.setText(R.string.incorrect_document);
                        break;
                    case TOO_MANY_DOCS:
                        numberPages.setText(R.string.too_many_docs);
                        break;
                }
            }

        }

        /**
         * UI changes when being selected
         *
         * @param selected yes or no?
         */
        @Override
        void setSelected(boolean selected) {
            if (isSelectable) {
                item.setBackgroundColor(selected ? colorPrimaryOpaque : colorBlackOpaque);
                selectedCb.setVisibility(selected ? View.VISIBLE : View.GONE);
            } else {
                numberPages.setVisibility(selected ? View.VISIBLE : View.INVISIBLE);
                thumbnail.setImageAlpha(selected ? 128 : 255);
                item.setAlpha(selected ? 0.5f : 1);
            }
        }
    }
}
