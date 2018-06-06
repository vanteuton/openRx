package com.example.wjmj2825.openrx

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.widget.AutoCompleteTextView

/**
 * Cette classe représente un AutoCompleteTextView qui affiche l'intégralité des choix disponibles dès le focus.
 * Le code a été trouvé sur Github ici :
 * https://stackoverflow.com/questions/15544943/show-all-items-in-autocompletetextview-without-writing-text
 */
class InstantAutoComplete : AutoCompleteTextView {

    constructor(context: Context) : super(context) {}

    constructor(arg0: Context, arg1: AttributeSet) : super(arg0, arg1) {}

    constructor(arg0: Context, arg1: AttributeSet, arg2: Int) : super(arg0, arg1, arg2) {}

    override fun enoughToFilter(): Boolean {
        return true
    }

    override fun onFocusChanged(focused: Boolean, direction: Int,
                                previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        if (focused) {
            performFiltering(text, 0)
        }
    }

}