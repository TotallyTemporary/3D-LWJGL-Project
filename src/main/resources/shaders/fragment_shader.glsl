#version 330 core
in vec2 pass_textureCoords;
flat in float pass_textureIndex;
out vec4 FragColor;

uniform sampler2DArray arrayTexture;

void main()
{
    FragColor = texture(arrayTexture, vec3(pass_textureCoords.xy, pass_textureIndex));
    // FragColor = vec4(pass_textureCoords.xy, 0.0, 1.0);
    // FragColor = texture(arrayTexture, vec2(pass_textureCoords.x, 1.0 - pass_textureCoords.y));
} 