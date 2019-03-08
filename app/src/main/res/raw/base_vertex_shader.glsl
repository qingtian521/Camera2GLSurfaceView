//顶点着色器
attribute vec4 aPosition; //顶点坐标
uniform mat4 uTextureMatrix; //
attribute vec4 aTextureCoordinate; //纹理坐标
varying vec2 vTextureCoord; //用来向片元着色器传递纹理坐标用的，片元着色器会根据这个坐标对图片进行取样，然后进行处理，然后我们就完成了图像一个小区域的处理，GPU会自动对于纹理的所有小区域进行处理，完成滤镜的操作。
void main()
{
  vTextureCoord = (uTextureMatrix * aTextureCoordinate).xy;
  gl_Position = aPosition;
}