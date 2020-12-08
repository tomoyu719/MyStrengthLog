import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import 'package:mystrengthlog/todo_bloc.dart';
import 'package:mystrengthlog/ui/todo_list.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {

  @override
  Widget build(BuildContext context) {

    return MultiProvider(
      providers: [
        Provider<TodoBloc>(
          create: (context) => TodoBloc(),
          dispose: (_, bloc) => bloc.dispose(),
        ),
      ],
      child: MaterialApp(
        title: 'demo',
        theme: ThemeData(
          primarySwatch: Colors.blue,
          visualDensity: VisualDensity.adaptivePlatformDensity,
        ),
        home: ListScreen(),
      ),
    );
  }
}


