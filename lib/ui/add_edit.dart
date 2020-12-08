import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import 'package:mystrengthlog/models.dart';
import 'package:mystrengthlog/todo_bloc.dart';

class AddEditScreen extends StatelessWidget {
  String id;
  AddEditScreen([this.id]);
  TextEditingController _titleController;
  TextEditingController _descriptionController;

  final _formKey = GlobalKey<FormState>();

  @override
  Widget build(BuildContext context) {
    final _bloc = Provider.of<TodoBloc>(context, listen: false);
    _bloc.getTodoById(id);
    return Scaffold(
      appBar: AppBar(
        title: const Text('title'),
      ),
      body: StreamBuilder<Todo>(
          stream: _bloc.todoStream,
          builder: (BuildContext context, AsyncSnapshot<Todo> todo) {
            if (!todo.hasData && id != null) {
              return Center(child:CircularProgressIndicator());
            }
            else{
              return Form(
                key: _formKey,
                child: Column(
                    children: [
                      TextFormField(
//                    initialValue: todo.data.title + "",
                      ),
                      TextFormField(
//                    initialValue: todo?.data.description + "",
                      ),
                      RaisedButton(
                        onPressed: () {
                          print(todo);
                        },
                      ),
                    ]
                ),
              );
            }
          }
      ),
    );
  }
}
