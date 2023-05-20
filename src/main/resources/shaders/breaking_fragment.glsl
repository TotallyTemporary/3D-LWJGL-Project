#version 330 core
in vec3 pass_textureCoords;
out vec4 FragColor;

uniform sampler2DArray arrayTexture;

void main()
{
    vec4 tex = texture(arrayTexture, pass_textureCoords);

    // for some unknown reason the texture doesn't have alpha.
    // so we discard values really close to the background colour 127/255 grey.
    const float halfBrightness = 0.4980392156862745098;
    if (abs(tex.r - halfBrightness) < 0.10) {
        discard;
    }

    FragColor = tex;
}