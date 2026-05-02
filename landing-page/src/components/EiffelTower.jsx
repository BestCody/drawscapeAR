import GLBModel from './GLBModel'

export default function EiffelTower(props) {
  return <GLBModel url="/models/eiffel.glb" height={500} {...props} />
}