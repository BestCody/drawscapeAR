import GLBModel from './GLBModel'

export default function GreatWall(props) {
  return <GLBModel url="/models/wall.glb" height={500} {...props} />
}