#extension GL_OES_EGL_image_external : require
precision mediump float;
uniform samplerExternalOES uTextureSampler; //源图像（相机预览、视频播放）
varying vec2 vTextureCoord; //是片元着色器传递过来的纹理坐标
void main()
{
  vec4 vCameraColor = texture2D(uTextureSampler, vTextureCoord);
  /*float fGrayColor = (0.3*vCameraColor.r + 0.59*vCameraColor.g + 0.11*vCameraColor.b);
  gl_FragColor = vec4(fGrayColor, fGrayColor, fGrayColor, 1.0);*/
  gl_FragColor = vec4(vCameraColor.r, vCameraColor.g, vCameraColor.b, 1.0);
  //gl_FragColor是OpenGL的内置变量，他是一个vec4类型，代表当前片元的RGBA值，每个元素都是0-1的浮点数
}
