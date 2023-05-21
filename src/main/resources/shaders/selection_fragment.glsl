#version 330 core
in vec3 pass_textureCoords;
out vec4 FragColor;

uniform sampler2DArray arrayTexture;
uniform float colourMultiplier;

void main()
{
    vec4 tex = texture(arrayTexture, pass_textureCoords);
    if (tex.a < 0.5) {
        discard;
    }
    FragColor = colourMultiplier * vec4(tex.rgb, 1);
}