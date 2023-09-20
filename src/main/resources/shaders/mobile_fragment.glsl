#version 330 core

in vec2 pass_textureCoords;

out vec4 FragColor;

uniform sampler2D basicTexture;

void main()
{
    vec4 tex = texture(basicTexture, pass_textureCoords);
    FragColor = tex;
}