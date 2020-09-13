
@param String abc

<html>
    <head>
        <title>Alexander's Automatic Barbershop Arranger (AABA)</title>

        <script src="http://moinejf.free.fr/js/abcweb-1.js"></script>
        <script src="http://moinejf.free.fr/js/js/snd-1.js"></script>
        <script src="http://moinejf.free.fr/js/js/follow-1.js"></script>
        <style type="text/css">
        	svg {display:block}
        </style>
    </head>
    <body>
        <h1>Alexander's Automatic Barbershop Arranger (AABA)</h1>

        <p>Welcome to AABA! Please enter a song below, in ABC format, and
        click "Submit". After a few moments, you will see an elementary barbershop
        arrangement of your music below.</p>

        <form action="/" method="post">
            <p><textarea name="input_abc" rows="10" cols="80"></textarea></p>
            <input type="submit" name="submit" />
        </form>

        <hr/>

@if( abc == null )
(placeholder)
@else
${abc}
@endif

    </body>
</html>

