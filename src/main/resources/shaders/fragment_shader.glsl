#version 330 core
in vec2 pass_textureCoords;
flat in float pass_textureIndex;
out vec4 FragColor;

uniform sampler2DArray arrayTexture;

void main()
{
    FragColor = texture(arrayTexture, vec3(pass_textureCoords.xy, pass_textureIndex));
}