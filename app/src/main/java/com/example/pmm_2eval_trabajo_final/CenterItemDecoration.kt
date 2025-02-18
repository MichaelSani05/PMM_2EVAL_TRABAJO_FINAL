package com.example.pmm_2eval_trabajo_final

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class CenterItemDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val itemCount = state.itemCount

        if (itemCount == 1) {
            // Si solo hay un elemento, centra el elemento
            outRect.left = spacing
            outRect.right = spacing
        } else {
            // Si hay varios elementos, aplica el espaciado normal
            if (position == 0) {
                outRect.left = spacing * 1 // Mayor margen para el primer elemento
                outRect.right = spacing / 2
            } else if (position == itemCount - 1) {
                outRect.left = spacing / 2
                outRect.right = spacing * 1 // Mayor margen para el último elemento
            } else {
                outRect.left = spacing / 2
                outRect.right = spacing / 2
            }
        }
    }
}