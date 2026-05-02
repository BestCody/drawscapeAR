import GLBModel from './GLBModel'

export default function StatueOfLiberty(props) {
  return <GLBModel url="/models/liberty.glb" height={500} {...props} />
}