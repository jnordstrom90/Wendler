package se.johan.wendler.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.widget.EditText;

import se.johan.wendler.R.styleable;
import se.johan.wendler.util.MinMaxInputFilter;

/**
 * EditText which will take an minimum and maximum value and apply it to a filter.
 */
public class FilterEditText extends EditText {

    /**
     * Constructor.
     */
    public FilterEditText(Context context, double min, double max) {
        super(context);
        setFilters(new InputFilter[]{new MinMaxInputFilter(min, max)});
    }

    /**
     * Constructor.
     */
    public FilterEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray attr = context.obtainStyledAttributes(attrs, styleable.MinMaxFilter);
        double min = attr.getFloat(styleable.MinMaxFilter_min, 1);
        double max = attr.getFloat(styleable.MinMaxFilter_max, 9999);
        setFilters(new InputFilter[]{new MinMaxInputFilter(min, max)});
        attr.recycle();
    }

    /**
     * Constructor.
     */
    public FilterEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray attr = context.obtainStyledAttributes(attrs, styleable.MinMaxFilter);
        double min = attr.getFloat(styleable.MinMaxFilter_min, 1);
        double max = attr.getFloat(styleable.MinMaxFilter_max, 9999);
        setFilters(new InputFilter[]{new MinMaxInputFilter(min, max)});
        attr.recycle();
    }
}
