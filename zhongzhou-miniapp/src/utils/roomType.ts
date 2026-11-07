import singleStandard from '../assets/rooms/single-standard.png'
import doubleRoom from '../assets/rooms/double.png'
import triple from '../assets/rooms/triple.png'
import quad from '../assets/rooms/quad.png'
import specialCare from '../assets/rooms/special-care.png'
import singleSuite from '../assets/rooms/single-suite.png'

export interface RoomTypeMeta {
  label: string
  image: string
}

/** 房型编码（biz_room.room_type，与字典 biz_room_type 一致）→ 展示名称与房型图片 */
const ROOM_TYPE_META: Record<string, RoomTypeMeta> = {
  '1': { label: '单人间', image: singleStandard },
  '2': { label: '双人间', image: doubleRoom },
  '3': { label: '三人间', image: triple },
  '4': { label: '四人间', image: quad },
  '5': { label: '套房', image: singleSuite },
  '6': { label: '特护房', image: specialCare },
}

const DEFAULT_META: RoomTypeMeta = { label: '双人间', image: doubleRoom }

export function getRoomTypeMeta(type?: string | null): RoomTypeMeta {
  return (type && ROOM_TYPE_META[type]) || DEFAULT_META
}
