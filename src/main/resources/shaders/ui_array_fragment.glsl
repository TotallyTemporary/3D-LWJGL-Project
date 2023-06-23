#version 330 core
in vec2 pass_textureCoords;
out vec4 FragColor;

uniform sampler2DArray arrayTexture;
uniform float uiIndex;

void main()
{
    vec4 tex = texture(arrayTexture, vec3(pass_textureCoords.xy, uiIndex));
    if (tex.a < 0.5f) {
      discard;
    }
    FragColor = tex;
}