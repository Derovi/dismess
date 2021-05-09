package by.dismess.core.chating.elements.id

import by.dismess.core.utils.UniqID
import by.dismess.core.utils.groupID
import by.dismess.core.utils.uniqID

data class ChunkID(
    val flowID: FlowID,
    val index: Int
) {
    val uniqID: UniqID = groupID(flowID.uniqID, index.uniqID)
}
