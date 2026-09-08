package com.suncommerz.associate.ui.orderitem

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.constraintlayout.compose.ConstraintLayout
import com.suncommerz.associate.R
import com.suncommerz.associate.domain.model.Store

@Composable
fun ListItemNearByStoresWhereOrderItemAvailable(
    nearByStore: Store,
    reservedStoreId: String?,
    orderItemInNearByStoreReserved: () -> Unit
) {
    Card(
        modifier = Modifier.padding(dimensionResource(R.dimen.padding_extra_small)),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.tertiaryContainer),
        onClick = {
            if (reservedStoreId != nearByStore.id) {
                orderItemInNearByStoreReserved()
            }
        },
        shape = RoundedCornerShape(dimensionResource(R.dimen.padding)),
        enabled = (reservedStoreId != nearByStore.id)
    ) {

        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.padding)),
        ) {
            val (nearbyStoreDetails, tapIndicationArrow) = createRefs()

            Column(
                modifier = Modifier
                    .constrainAs(nearbyStoreDetails) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        bottom.linkTo(parent.bottom)
                    }
                    .wrapContentHeight()
                    .padding(dimensionResource(R.dimen.padding_small))
            ) {
                Text(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(dimensionResource(R.dimen.padding_extra_small)),
                    text = nearByStore.name,
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(dimensionResource(R.dimen.padding_extra_small)),
                    text = nearByStore.location.toString(),
                    style = MaterialTheme.typography.bodySmall
                )
            }


            IconButton(
                modifier = Modifier.constrainAs(tapIndicationArrow) {
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                },
                onClick = {}
            ) {
                Icon(
                    imageVector = (reservedStoreId != nearByStore.id).run {
                        if(this) Icons.Default.Add else Icons.Default.Check
                    },
                    contentDescription = stringResource(R.string.content_description_icon_pickup_item)
                )
            }
        }
    }
}