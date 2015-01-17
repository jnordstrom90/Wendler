package se.johan.wendler.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.telly.mrvector.MrVector;

import se.johan.wendler.R;

/**
 * View displaying a info text regarding empty lists.
 */
public class NoItemsView extends RelativeLayout {

    /**
     * Constructor.
     */
    public NoItemsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.no_items_added, this, true);
        initViews(attrs);
    }

    /**
     * Initialize the views.
     */
    private void initViews(AttributeSet attrs) {

        TypedArray attr = getContext().obtainStyledAttributes(attrs, R.styleable.NoItemsView);

        int color = attr.getColor(
                R.styleable.NoItemsView_imageColor,
                getResources().getColor(R.color.blue));
        ((ImageView) findViewById(R.id.image)).setImageDrawable(
                MrVector.inflate(getResources(), R.drawable.emoticon_sad));
        ((ImageView) findViewById(R.id.image)).setColorFilter(color, PorterDuff.Mode.MULTIPLY);

        ((TextView) findViewById(R.id.text))
                .setText(attr.getText(R.styleable.NoItemsView_infoText));

        attr.recycle();
    }

}
